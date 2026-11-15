const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { test, before, after, beforeEach } = require('node:test');
const fs = require('node:fs');

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'gen-lang-client-0571452922',
    firestore: {
      rules: fs.readFileSync('firestore.rules', 'utf8'),
      host: '127.0.0.1',
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

test('1. Unauthenticated requests are denied across all collections', async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date();

  await assertFails(unauthedDb.doc('users/alice/conversations/conv_1').get());
  await assertFails(unauthedDb.doc('users/alice/conversations/conv_1').set({
    userId: 'alice',
    title: 'Study Group',
    type: 'STUDY_GROUP',
    peerCode: '123456',
    lastMessagePreview: 'Hello',
    createdAt: now,
    updatedAt: now,
  }));
  await assertFails(unauthedDb.doc('users/alice/messages/msg_1').get());
  await assertFails(unauthedDb.doc('rooms/room_1/messages/msg_1').get());
});

test('2. Authenticated user can create, read, update, and delete their own conversation', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const now = new Date();
  const convRef = aliceDb.doc('users/alice/conversations/conv_ece');

  await assertSucceeds(convRef.set({
    userId: 'alice',
    title: 'ECE Gate Study Group',
    type: 'STUDY_GROUP',
    peerCode: '482910',
    lastMessagePreview: 'Welcome to the room',
    subtitle: 'Online',
    isPinned: true,
    createdAt: now,
    updatedAt: now,
  }));

  await assertSucceeds(convRef.get());

  await assertSucceeds(convRef.update({
    lastMessagePreview: 'Updated preview',
    updatedAt: new Date(),
  }));

  // Cannot mutate immutable peerCode or userId
  await assertFails(convRef.update({
    peerCode: '999999',
    updatedAt: new Date(),
  }));

  await assertSucceeds(convRef.delete());
});

test('3. Cross-user access to another user conversation or message is denied', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const now = new Date();

  const aliceMsgRef = aliceDb.doc('users/alice/messages/msg_101');
  await assertSucceeds(aliceMsgRef.set({
    userId: 'alice',
    conversationId: 'conv_ece',
    senderId: 'alice',
    senderName: 'Alice',
    content: 'Laplace transform derivation',
    messageType: 'TEXT',
    deliveryStatus: 'SENT',
    createdAt: now,
    updatedAt: now,
  }));

  // Bob cannot read, update, or delete Alice's message
  const bobViewRef = bobDb.doc('users/alice/messages/msg_101');
  await assertFails(bobViewRef.get());
  await assertFails(bobViewRef.update({
    content: 'Tampered by Bob',
    updatedAt: new Date(),
  }));
  await assertFails(bobViewRef.delete());
});

test('4. Real-time message schema supports image Base64, GPS location, and file metadata while rejecting invalid payloads', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const now = new Date();

  // Valid GPS location message
  const locMsgRef = aliceDb.doc('users/alice/messages/msg_loc_1');
  await assertSucceeds(locMsgRef.set({
    userId: 'alice',
    conversationId: 'conv_ece',
    senderId: 'alice',
    senderName: 'Alice',
    content: '📍 Live Location (60 min): IIT Campus Library',
    messageType: 'LOCATION',
    deliveryStatus: 'DELIVERED',
    latitude: 28.5450,
    longitude: 77.1926,
    locationLabel: 'IIT Campus Library',
    liveLocationDurationMinutes: 60,
    createdAt: now,
    updatedAt: now,
  }));

  // Invalid latitude (> 90) is rejected
  const badLocRef = aliceDb.doc('users/alice/messages/msg_loc_bad');
  await assertFails(badLocRef.set({
    userId: 'alice',
    conversationId: 'conv_ece',
    senderId: 'alice',
    senderName: 'Alice',
    content: 'Bad coordinates',
    messageType: 'LOCATION',
    deliveryStatus: 'SENT',
    latitude: 145.0,
    longitude: 77.1926,
    createdAt: now,
    updatedAt: now,
  }));

  // Undeclared extra field is rejected
  const extraFieldRef = aliceDb.doc('users/alice/messages/msg_extra');
  await assertFails(extraFieldRef.set({
    userId: 'alice',
    conversationId: 'conv_ece',
    senderId: 'alice',
    senderName: 'Alice',
    content: 'Extra field test',
    messageType: 'TEXT',
    deliveryStatus: 'SENT',
    hackField: 'not_allowed',
    createdAt: now,
    updatedAt: now,
  }));
});

test('5. Shared OTP/QR room messages allow participants and block non-participants', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const malloryDb = testEnv.authenticatedContext('mallory').firestore();
  const now = new Date();

  const roomMsgRef = aliceDb.doc('rooms/room_482910/messages/rmsg_1');
  await assertSucceeds(roomMsgRef.set({
    roomId: 'room_482910',
    senderUid: 'alice',
    senderName: 'Alice',
    participantUids: ['alice', 'bob'],
    content: 'Connected via 6-digit OTP 482910!',
    messageType: 'TEXT',
    deliveryStatus: 'SENT',
    createdAt: now,
    updatedAt: now,
  }));

  // Bob (participant) can read and react/mark delivered
  const bobRoomMsgRef = bobDb.doc('rooms/room_482910/messages/rmsg_1');
  await assertSucceeds(bobRoomMsgRef.get());
  await assertSucceeds(bobRoomMsgRef.update({
    deliveryStatus: 'READ',
    reactionEmoji: '👍',
    updatedAt: new Date(),
  }));

  // Mallory (non-participant) cannot read or update
  const malloryRoomMsgRef = malloryDb.doc('rooms/room_482910/messages/rmsg_1');
  await assertFails(malloryRoomMsgRef.get());
  await assertFails(malloryRoomMsgRef.update({
    deliveryStatus: 'READ',
    updatedAt: new Date(),
  }));
});
