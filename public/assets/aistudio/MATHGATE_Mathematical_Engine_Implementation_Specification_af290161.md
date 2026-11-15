# MATHGATE --- Mathematical Engine Implementation Specification

**Document type:** Technical Product & Engineering Specification\
**Version:** 1.1\
**Platform:** Native Android\
**Language:** Kotlin\
**UI:** Jetpack Compose + Material 3\
**Architecture:** Clean Architecture + MVVM\
**Operation:** Fully offline, on-device\
**AI / external APIs:** None

------------------------------------------------------------------------

## 1. Purpose

This document converts the MATHGATE mathematical-only product plan into
an implementation-ready specification.

MATHGATE generates mathematical questions locally, evaluates answers
locally, learns each user's mathematical strengths and weaknesses, and
adjusts subsequent questions using deterministic rules.

The first implementation should prioritize correctness, explainability,
predictable performance and a polished blue-and-white Android interface.

### Product principles

1.  No AI model, AI API, API key or cloud dependency.
2.  Every question must be generated and evaluated on the device.
3.  Randomness controls question variation; constraints control validity
    and difficulty.
4.  Adaptation is performed separately for each mathematical skill.
5.  Users can inspect explanations and review mistakes.
6.  Learning history survives app restarts.
7.  The user can pause, reset or export their learning data.
8.  The app must not claim that faster answers always mean better
    mathematical understanding.

------------------------------------------------------------------------

## 2. Locked baseline configuration

Until the user changes these settings, use the following recommended
defaults:

  -----------------------------------------------------------------------
  Decision                            Default
  ----------------------------------- -----------------------------------
  Difficulty                          Hybrid: adaptive with manual
                                      override

  Answer input                        Both numeric keypad and multiple
                                      choice

  Challenge experience                Continuous challenges and fixed
                                      practice sessions

  Initial categories                  Addition, subtraction,
                                      multiplication, division,
                                      percentages, fractions and decimals

  Default practice session            10 questions

  Continuous challenge                One question at a time

  Adaptive history window             10 recent attempts per skill

  Recent duplicate history            100 fingerprints

  Initial unlock challenge            One short question

  Offline requirement                 All core features work without
                                      network

  Theme                               Light blueprint-inspired blue and
                                      white
  -----------------------------------------------------------------------

These are defaults, not irreversible product constraints. Expose
relevant preferences in Settings.

------------------------------------------------------------------------

## 3. Scope

### Included in the mathematical MVP

-   Random arithmetic question generation.
-   Addition, subtraction, multiplication and exact division.
-   Percentages with controlled integer or decimal results.
-   Fraction simplification and basic fraction operations.
-   Decimal operations.
-   Five difficulty bands.
-   Per-category skill profiles.
-   Adaptive difficulty.
-   Weighted topic selection.
-   Answer validation.
-   Step-by-step explanations.
-   Recent duplicate avoidance.
-   Attempt history and local analytics.
-   Continuous and fixed-length practice.
-   Local review queue.
-   Offline persistence.

### Excluded from the first mathematical MVP

-   AI question generation.
-   Online question APIs.
-   User accounts and cloud synchronization.
-   Online leaderboards.
-   Social features.
-   Advanced geometry and trigonometry.
-   Symbolic algebra engine.
-   Speech recognition.
-   Automatic monitoring of other applications.

The app-monitoring and unlock system can be integrated after the
mathematical engine is reliable.

------------------------------------------------------------------------

## 4. Functional requirements

  -----------------------------------------------------------------------
  ID                      Requirement             Priority
  ----------------------- ----------------------- -----------------------
  FR-01                   Generate a valid        P0
                          question for a selected 
                          category                

  FR-02                   Generate questions      P0
                          within configured       
                          difficulty constraints  

  FR-03                   Validate the user's     P0
                          answer locally          

  FR-04                   Show a correct          P0
                          explanation for each    
                          generated question      

  FR-05                   Record response time    P0
                          and correctness         

  FR-06                   Maintain an independent P0
                          skill profile for each  
                          category                

  FR-07                   Adjust difficulty based P0
                          on recent performance   

  FR-08                   Avoid recent duplicate  P0
                          questions               

  FR-09                   Persist progress across P0
                          restarts                

  FR-10                   Support numeric answer  P0
                          entry                   

  FR-11                   Support multiple-choice P1
                          questions               

  FR-12                   Support fixed-length    P1
                          sessions                

  FR-13                   Support continuous      P1
                          challenge mode          

  FR-14                   Schedule review of      P1
                          difficult question      
                          types                   

  FR-15                   Provide category-level  P1
                          analytics               

  FR-16                   Export and reset local  P2
                          learning data           
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 5. Recommended architecture

Use a single-activity Compose application with independent feature
modules/packages. Do not put generation, evaluation, database access and
UI state in `MainActivity`.

``` text
com.mathgate.app/
├── MainActivity.kt
├── MathGateApplication.kt
│
├── core/
│   ├── designsystem/
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   ├── Dimensions.kt
│   │   └── MathGateTheme.kt
│   ├── navigation/
│   ├── common/
│   └── time/
│
├── domain/
│   ├── model/
│   ├── generator/
│   ├── evaluator/
│   ├── adaptive/
│   ├── selector/
│   ├── repository/
│   └── usecase/
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── mapper/
│   └── repository/
│
├── feature/
│   ├── challenge/
│   ├── practice/
│   ├── explore/
│   ├── categories/
│   ├── saved/
│   ├── progress/
│   └── settings/
│
└── di/
```

### Layer responsibilities

**Presentation** - Compose screens and reusable UI components. -
ViewModels and immutable UI state. - User input and navigation. - No
direct SQL or mathematical generation logic.

**Domain** - Question generation rules. - Mathematical calculations. -
Answer evaluation. - Difficulty adjustment. - Skill tracking rules. -
Topic selection. - No Android UI dependencies.

**Data** - Room entities and DAOs. - Repository implementations. -
Persistence and history. - Data mapping.

### Data flow

``` text
Compose Screen
    |
    v
ViewModel
    |
    v
Use Case
    |
    +--> Topic Selector
    |
    +--> Adaptive Controller
    |
    +--> Question Generator
    |
    +--> Question Validator
    |
    v
Repository
    |
    v
Room Database
```

------------------------------------------------------------------------

## 6. Domain models

Use typed models instead of arbitrary strings for categories, difficulty
and answer formats.

``` kotlin
enum class MathCategory {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    PERCENTAGE,
    FRACTION,
    DECIMAL
}

enum class Difficulty {
    BEGINNER,
    BASIC,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

enum class AnswerFormat {
    INTEGER,
    DECIMAL,
    FRACTION,
    MULTIPLE_CHOICE
}

data class MathQuestion(
    val id: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val prompt: String,
    val answer: String,
    val explanation: String,
    val expectedTimeSeconds: Int,
    val fingerprint: String,
    val generatorVersion: Int
)

data class QuestionAttempt(
    val id: String,
    val questionId: String,
    val category: MathCategory,
    val difficulty: Difficulty,
    val submittedAnswer: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val attemptedAtEpochMs: Long
)

data class SkillProfile(
    val category: MathCategory,
    val skillScore: Float,
    val accuracy: Float,
    val averageResponseTimeMs: Long,
    val currentDifficulty: Difficulty,
    val correctStreak: Int,
    val wrongStreak: Int,
    val attemptsCount: Int
)
```

### Important model rules

-   Store exact answers as canonical strings or exact numeric
    representations.
-   Do not use `Double` equality for exact decimal or fraction
    validation.
-   Use `Long` only when range analysis proves it is sufficient.
-   Use `BigInteger` for large integer calculations.
-   Use `BigDecimal` for decimal calculations.
-   Represent fractions using numerator and denominator integers or
    `BigInteger`.
-   Store the generator version so historical questions remain
    interpretable after generator changes.

For production, a question should also retain structured generation
parameters. For example, multiplication can retain operands `a` and `b`;
a fraction question can retain its numerators and denominators. This
enables reliable explanation rendering and error analysis.

------------------------------------------------------------------------

## 7. Generator interface

Every category-specific generator should implement one contract.

``` kotlin
interface QuestionGenerator {
    fun generate(
        difficulty: Difficulty,
        random: kotlin.random.Random
    ): MathQuestion
}
```

Create a generator registry:

``` kotlin
class GeneratorRegistry(
    private val generators: Map<MathCategory, QuestionGenerator>
) {
    fun generatorFor(category: MathCategory): QuestionGenerator =
        requireNotNull(generators[category]) {
            "No generator registered for $category"
        }
}
```

Use dependency injection to provide the registry and a random provider.

### Randomness policy

-   Use Kotlin's random generator for normal production question
    variation.
-   Inject the random source into generators.
-   Use a seeded random source in unit tests to make failures
    reproducible.
-   Do not use random values as the source of mathematical correctness.
-   Generate valid operands first, then calculate the answer
    deterministically.

------------------------------------------------------------------------

## 8. Difficulty specification

Difficulty must be defined per generator, not just by the size of a
number.

A multiplication question with two small operands can still be difficult
if it requires a less familiar mental strategy. Therefore, use multiple
difficulty dimensions:

-   Operand magnitude.
-   Number of digits.
-   Carrying or borrowing.
-   Number of operations.
-   Presence of decimals or fractions.
-   Whether the result is exact.
-   Expected solving time.
-   Number of conceptual steps.

### Initial arithmetic ranges

These are starting ranges and must be calibrated after testing.

  Difficulty     Addition/subtraction operands   Multiplication operands
  -------------- ------------------------------- -------------------------
  Beginner       1--9                            2--9
  Basic          10--99                          2--12
  Intermediate   100--999                        10--30
  Advanced       1,000--9,999                    20--99
  Expert         10,000--99,999                  50--999

For subtraction, decide whether negative answers are allowed. The
initial default should be non-negative results until the user enables
signed arithmetic.

For division, generate divisor and quotient first, then calculate the
dividend:

``` kotlin
val divisor = random.nextInt(2, 20)
val quotient = random.nextInt(2, 100)
val dividend = divisor * quotient
```

This guarantees an exact integer quotient.

### Difficulty configuration

Keep ranges in configuration objects rather than scattering numeric
constants throughout generator code.

``` kotlin
data class IntegerRangeConfig(
    val operandMin: Int,
    val operandMax: Int,
    val allowNegativeResult: Boolean = false
)
```

------------------------------------------------------------------------

## 9. Adaptive controller

Use a transparent rule-based controller. Do not label it AI or machine
learning.

Maintain an independent profile for every category.

### Metrics

-   Recent accuracy.
-   Average response time.
-   Expected response time for the question.
-   Consecutive correct answers.
-   Consecutive incorrect answers.
-   Attempts count.
-   Last practice timestamp.
-   Current difficulty.
-   Recent performance trend.

### Initial adaptation rules

  -----------------------------------------------------------------------
  Condition                           Action
  ----------------------------------- -----------------------------------
  5 consecutive correct, high         Increase one difficulty level
  accuracy and appropriately fast     

  3 consecutive correct               Consider increase after window
                                      evaluation

  One incorrect answer                Do not immediately lower difficulty

  2 consecutive incorrect             Reassess recent performance

  3 consecutive incorrect             Lower one level or offer a hint

  Correct but slower than expected    Maintain level; practice same skill

  Fast but inaccurate                 Prioritize accuracy

  Insufficient attempts               Keep default difficulty
  -----------------------------------------------------------------------

Use a rolling window of 8--12 attempts per skill. Do not let one
unusually fast answer or one accidental mistake cause a large difficulty
jump.

### Initial skill score

A provisional score may combine:

``` text
Skill Score =
    50% recent accuracy
  + 20% normalized speed
  + 20% recent trend
  + 10% consistency
```

The speed component must be normalized against the expected time for
that question's difficulty and format. Do not compare a simple addition
question with a multi-step fraction problem.

Treat this score as a learning indicator, not a scientifically validated
measure of intelligence.

### Hysteresis

Prevent difficulty oscillation:

-   Require multiple strong attempts before increasing.
-   Require repeated evidence of struggle before decreasing.
-   Change by no more than one level at a time.
-   Allow the user to set minimum and maximum difficulty limits.
-   Provide a manual override.

------------------------------------------------------------------------

## 10. Topic selection

The selector should balance weakness recovery, revision and variety.

Initial distribution:

  Topic group           Share
  ------------------- -------
  Weak skills             50%
  Developing skills       30%
  Strong skills           20%

These percentages are configurable.

A selection score can use:

``` text
Selection Weight =
    0.45 * Weakness
  + 0.30 * Review Urgency
  + 0.15 * Time Since Practice
  + 0.10 * Variety Bonus
```

Normalize weights into probabilities before random selection.

Rules:

1.  Do not select disabled categories.
2.  Respect the user's selected category filters.
3.  Prioritize overdue review items when appropriate.
4.  Avoid repeating the same category excessively.
5.  Keep a small proportion of mastered topics for retention.
6.  Fall back safely if a category has no eligible question generator.

------------------------------------------------------------------------

## 11. Duplicate prevention

Create a canonical fingerprint for every generated question.

Examples:

``` text
MULTIPLICATION:16:25
ADDITION:16:25
SUBTRACTION:25:16
DIVISION:400:16
```

Normalize commutative operations:

-   Addition: sort operands before fingerprinting.
-   Multiplication: sort operands before fingerprinting.
-   Subtraction: preserve order.
-   Division: preserve order.

Maintain the last 100 fingerprints in local storage.

Generation algorithm:

1.  Generate a candidate.
2.  Validate the candidate.
3.  Build its fingerprint.
4.  Check recent history.
5.  If duplicated, retry a bounded number of times.
6.  If retries are exhausted, widen the parameter range or use a
    controlled fallback.

Never use an unbounded retry loop.

------------------------------------------------------------------------

## 12. Mathematical correctness and evaluation

Use one source of truth for question generation, answer calculation,
validation and explanation.

### Integer arithmetic

Use `Long` or `BigInteger` with explicit overflow protection.

### Decimal arithmetic

Use `BigDecimal` with a defined scale and rounding policy.

Do not compare decimal values using `Double ==`.

### Fractions

Represent a fraction as:

``` kotlin
data class Fraction(
    val numerator: BigInteger,
    val denominator: BigInteger
)
```

Normalize:

-   Denominator must not be zero.
-   Move a negative sign to the numerator.
-   Divide numerator and denominator by their GCD.
-   Define whether equivalent fraction formats are accepted.

For example, `2/4` and `1/2` should compare as equivalent after
normalization.

### Multiple-choice distractors

Generate distractors from plausible error patterns:

-   Addition/subtraction confusion.
-   Carrying errors.
-   Multiplication table errors.
-   Incorrect denominator operations.
-   Decimal-place mistakes.

Ensure: - Exactly one correct option. - No duplicate options. - All
options are valid for the answer format. - Correct option position is
randomized. - The correct answer is not consistently in the same
position.

------------------------------------------------------------------------

## 13. Explanation engine

Do not store only a generic explanation string when a procedural
explanation can be generated from the operands.

Example:

Question: `25 × 16`

Explanation:

``` text
25 × 16
= 25 × (4 × 4)
= (25 × 4) × 4
= 100 × 4
= 400
```

For each generator, define an explanation strategy.

  Generator        Explanation strategy
  ---------------- ----------------------------------------
  Addition         Column addition or decomposition
  Subtraction      Regrouping or compensation
  Multiplication   Distributive property or decomposition
  Division         Quotient × divisor verification
  Percentage       Convert percentage to fraction
  Fraction         Find common denominator or simplify
  Decimal          Align decimal places

A question must not be displayed if its answer or explanation fails
validation.

------------------------------------------------------------------------

## 14. Room database design

Recommended entities:

### SkillProfileEntity

``` text
category (primary key)
skillScore
accuracy
averageResponseTimeMs
currentDifficulty
correctStreak
wrongStreak
attemptsCount
lastPracticedAt
```

### QuestionAttemptEntity

``` text
id (primary key)
questionId
category
difficulty
submittedAnswer
isCorrect
responseTimeMs
attemptedAt
```

### GenerationHistoryEntity

``` text
fingerprint (primary key)
category
generatedAt
generatorVersion
```

### ReviewQueueEntity

``` text
id (primary key)
category
conceptKey
reviewAt
priority
failureCount
```

### DailyProgressEntity

``` text
date (primary key)
attemptedCount
correctCount
totalXp
practiceDurationMs
```

Do not permanently save every unused generated question. Store completed
attempts, compact fingerprints and review metadata. Store full question
snapshots for attempted or bookmarked questions when necessary.

------------------------------------------------------------------------

## 15. Repository and use-case contracts

Suggested use cases:

``` text
GenerateNextQuestionUseCase
SubmitAnswerUseCase
EvaluateAnswerUseCase
UpdateSkillProfileUseCase
AdjustDifficultyUseCase
SelectNextCategoryUseCase
GetSkillOverviewUseCase
GetPracticeHistoryUseCase
GetReviewQueueUseCase
BookmarkQuestionUseCase
ResetLearningProfileUseCase
ExportLearningDataUseCase
```

Repository interfaces:

``` kotlin
interface MathLearningRepository {
    suspend fun getSkillProfiles(): List<SkillProfile>
    suspend fun getSkillProfile(category: MathCategory): SkillProfile?
    suspend fun saveAttempt(attempt: QuestionAttempt)
    suspend fun updateSkillProfile(profile: SkillProfile)
    suspend fun getRecentFingerprints(limit: Int): Set<String>
    suspend fun saveFingerprint(fingerprint: String)
}
```

All database operations should be asynchronous. Use coroutines and
transactions when an attempt, skill profile and daily statistics must
update together.

------------------------------------------------------------------------

## 16. UI/UX specification

### Design direction

Modern Technical / Blueprint / Minimal Developer UI.

### Colors

  Token            Hex
  ---------------- -----------
  Primary Blue     `#003DA5`
  Secondary Blue   `#1647BB`
  Light Blue       `#EAF1FF`
  White            `#FFFFFF`
  Surface          `#F7F9FE`
  Border Blue      `#A9C1EF`
  Light Border     `#E1E9F7`
  Primary Text     `#12336F`
  Secondary Text   `#6480B6`
  Muted Text       `#8FA6D1`
  Success          `#13A66A`
  Warning          `#FFB91C`
  Error            `#FF575F`
  Accent Purple    `#8560E9`

### Typography

-   Barlow Condensed: screen and section headings.
-   Roboto Condensed: body and card text.
-   IBM Plex Mono: technical values and compact statistics.

Bundle font files locally. Do not rely on downloading fonts at runtime.

### Layout

-   White background.
-   Thin blue borders.
-   Compact rectangular cards.
-   4--6dp corner radii.
-   16dp horizontal screen padding.
-   48dp minimum touch targets.
-   Subtle shadows only.
-   No glassmorphism.
-   No heavy gradients.
-   No oversized decorative cards.

### Bottom navigation

1.  Home
2.  Explore
3.  Categories
4.  Saved
5.  Settings

### Challenge screen hierarchy

1.  Category and difficulty label.
2.  Question progress.
3.  Large equation.
4.  Answer input.
5.  Numeric keypad or MCQ options.
6.  Submit button.
7.  Correct/incorrect feedback.
8.  Explanation.
9.  XP and progress summary.

The question should be the strongest visual element.

------------------------------------------------------------------------

## 17. Interaction and accessibility

-   Use native Compose ripple feedback.
-   Animate answer feedback subtly.
-   Avoid excessive motion.
-   Support system font scaling.
-   Use semantic labels for icons and controls.
-   Ensure text contrast.
-   Provide a clear focus state.
-   Support keyboard input where available.
-   Do not rely on color alone to indicate correctness.
-   Support portrait and landscape layouts.
-   Respect system insets and edge-to-edge requirements.

------------------------------------------------------------------------

## 18. Offline and privacy requirements

The mathematical MVP must not require internet permission for its core
features.

Keep: - Questions on-device. - Answers on-device. - Skill profiles
on-device. - Attempt history on-device. - Preferences on-device. -
Explanations on-device.

Do not collect: - Monitored app content. - Keystrokes. - Screenshots. -
Unnecessary personal data. - Network analytics without a separately
justified and disclosed feature.

Provide: - Clear data reset. - Local data export. - Transparent privacy
information. - No mandatory registration. - No mandatory cloud backup.

Verify the final merged Android manifest to ensure no unnecessary
network permission is introduced by dependencies.

------------------------------------------------------------------------

## 19. Testing strategy

### Unit tests

-   Each generator produces valid expressions.
-   Each answer matches independently calculated expected values.
-   Each difficulty respects its parameter constraints.
-   Fraction normalization is correct.
-   Decimal comparisons follow the selected precision policy.
-   Fingerprints normalize commutative operands.
-   Adaptive rules follow the expected transitions.
-   Topic selection respects category filters.
-   Review scheduling calculates the expected due date.

### Property-based tests

For a large set of generated inputs:

-   `a + b` must equal the generated answer.
-   `a - b` must equal the generated answer.
-   `a * b` must equal the generated answer.
-   For exact division, `dividend % divisor == 0`.
-   `dividend / divisor` must equal the generated quotient.
-   Fraction numerator and denominator must be normalized.
-   Every MCQ must have exactly one correct option.

### UI tests

-   App opens directly into a challenge.
-   Numeric keypad accepts and deletes input.
-   Submit button validates the response.
-   Correct and incorrect feedback are distinct.
-   Explanation expands and collapses.
-   Bottom navigation works.
-   Skill statistics update after submission.
-   User settings persist after restart.

### Performance tests

-   Measure cold launch.
-   Measure question generation latency.
-   Measure database write latency.
-   Measure Compose frame rendering.
-   Test long histories.
-   Test 100,000 generated arithmetic questions.
-   Test repeated generation and duplicate filtering.

------------------------------------------------------------------------

## 20. Development phases

### Phase 1 --- Compose foundation

-   Create Android Studio project.
-   Configure design tokens.
-   Create navigation.
-   Implement Home, Explore, Categories, Saved and Settings screens.
-   Build reusable cards, buttons, keypad and progress indicators.

**Exit criterion:** All screens navigate and the challenge UI accepts
input using mock questions.

### Phase 2 --- Arithmetic generators

-   Addition generator.
-   Subtraction generator.
-   Multiplication generator.
-   Division generator.
-   Answer evaluator.
-   Explanation generator.
-   Unit and property tests.

**Exit criterion:** Random questions are valid and answers are verified.

### Phase 3 --- Adaptive controller

-   Skill profile model.
-   Performance metrics.
-   Difficulty rules.
-   Category selection.
-   Manual override.
-   Adaptive tests.

**Exit criterion:** Difficulty responds to category-specific performance
without oscillation.

### Phase 4 --- Persistence

-   Room database.
-   DAOs and repositories.
-   Attempt history.
-   Skill persistence.
-   Duplicate history.
-   Review queue.

**Exit criterion:** Progress survives force-stop and device restart.

### Phase 5 --- Additional mathematics

-   Percentages.
-   Fractions.
-   Decimals.
-   Powers and roots.
-   Mixed operations.

**Exit criterion:** Each generator passes correctness and difficulty
tests.

### Phase 6 --- Learning experience

-   XP.
-   Streaks.
-   Analytics.
-   Mistake review.
-   Bookmarks.
-   Fixed sessions and continuous mode.

**Exit criterion:** Complete offline mathematics learning experience.

### Phase 7 --- Integration

-   Integrate the mathematical engine with MATHGATE app-usage
    monitoring.
-   Select short questions for unlock challenges.
-   Preserve the same skill profile.
-   Test challenge and unlock lifecycle.

**Exit criterion:** Mathematical challenges work as the cognitive gate
for selected applications.

------------------------------------------------------------------------

## 21. MVP acceptance criteria

The mathematical MVP is ready for beta testing when:

-   [ ] App launches directly into a generated mathematical question.
-   [ ] No internet connection is needed.
-   [ ] Seven initial categories are supported or intentionally
    feature-gated.
-   [ ] Questions are generated within valid difficulty constraints.
-   [ ] Correct answers are calculated deterministically.
-   [ ] Explanations match the correct answers.
-   [ ] Numeric keypad works.
-   [ ] Multiple-choice mode works.
-   [ ] Adaptive difficulty is category-specific.
-   [ ] Manual difficulty override works.
-   [ ] Recent duplicate prevention works.
-   [ ] Attempts and skill profiles persist locally.
-   [ ] Review questions can be scheduled.
-   [ ] Users can reset and export their learning data.
-   [ ] No unnecessary network dependency exists.
-   [ ] UI passes accessibility and responsive-layout checks.

------------------------------------------------------------------------

## 22. Final engineering decisions

  Area                   Decision
  ---------------------- ---------------------------------------
  Language               Kotlin
  UI                     Jetpack Compose
  Architecture           Clean Architecture + MVVM
  Random generation      Kotlin Random, injected
  Question logic         Deterministic mathematical algorithms
  Adaptation             Rule-based, category-specific
  Persistence            Room
  Preferences            DataStore
  Concurrency            Kotlin Coroutines
  Dependency injection   Hilt
  Navigation             Navigation Compose
  Networking             Not required
  AI                     None
  Backend                None
  Offline operation      Full

### Final product principle

MATHGATE should not merely produce random sums. It should generate
mathematically valid, varied and appropriately challenging problems,
understand the user's demonstrated performance through local statistics,
and select the next problem using transparent rules.

**The first engineering milestone is a tested, standalone Kotlin
mathematical engine. Build and verify that engine before connecting it
to the app-monitoring and unlock system.**
