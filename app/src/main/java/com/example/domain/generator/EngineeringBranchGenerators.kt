package com.example.domain.generator

import com.example.domain.model.AnswerFormat
import com.example.domain.model.Difficulty
import com.example.domain.model.MathCategory
import com.example.domain.model.MathQuestion
import com.example.domain.model.QuestionMetadata
import com.example.domain.model.QuestionStyle
import java.util.UUID
import kotlin.random.Random

private fun buildEngIntQuestion(
    category: MathCategory,
    difficulty: Difficulty,
    prompt: String,
    answer: Long,
    steps: List<String>,
    expectedSeconds: Int,
    fingerprint: String,
    complexityTier: Int,
    style: QuestionStyle,
    random: Random
): MathQuestion {
    val options = DistractorBuilder.integerOptions(answer, allowNegative = answer < 0, random = random)
    return MathQuestion(
        id = UUID.randomUUID().toString(),
        category = category,
        difficulty = difficulty,
        answerFormat = AnswerFormat.INTEGER,
        prompt = prompt,
        answer = answer.toString(),
        explanationSteps = steps,
        explanation = steps.joinToString("\n"),
        options = options,
        expectedTimeSeconds = expectedSeconds,
        fingerprint = "$fingerprint:T$complexityTier:${style.name}",
        generatorVersion = 1,
        metadata = QuestionMetadata.MultiStepMeta(prompt, answer.toString()),
        complexityTier = complexityTier,
        questionStyle = style
    )
}

private data class ScienceMcqItem(
    val subject: String,
    val minDifficulty: Int,
    val prompt: String,
    val answer: String,
    val options: List<String>,
    val supportingFacts: List<String>
)

/**
 * School Science generator (QASC CC-BY-4.0 aligned + Physics, Chemistry, Biology reasoning)
 * using tappable 4-option MULTIPLE_CHOICE cards.
 */
class SchoolScienceGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.SCHOOL_SCIENCE
    override val version: Int = 1

    private val bank = listOf(
        ScienceMcqItem(
            "BIOLOGY", 1,
            "What cellular process converts light energy into chemical energy in green plants?",
            "Photosynthesis",
            listOf("Respiration", "Photosynthesis", "Transpiration", "Fermentation"),
            listOf(
                "Chloroplasts in green plants absorb sunlight.",
                "Photosynthesis uses sunlight, water, and CO₂ to synthesize glucose."
            )
        ),
        ScienceMcqItem(
            "PHYSICS", 1,
            "Which physical property measures the amount of matter in an object regardless of gravity?",
            "Mass",
            listOf("Weight", "Volume", "Mass", "Density"),
            listOf(
                "Mass is the quantity of matter contained in a body.",
                "Unlike weight, mass remains constant everywhere in the universe."
            )
        ),
        ScienceMcqItem(
            "CHEMISTRY", 2,
            "What gas is released when a dilute acid reacts with an active metal such as zinc?",
            "Hydrogen",
            listOf("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"),
            listOf(
                "Active metals displace hydrogen ions from dilute acids.",
                "Zn + 2HCl → ZnCl₂ + H₂↑ releases hydrogen gas."
            )
        ),
        ScienceMcqItem(
            "BIOLOGY", 2,
            "Which part of the human eye controls the size of the pupil to regulate light entry?",
            "Iris",
            listOf("Iris", "Cornea", "Retina", "Optic nerve"),
            listOf(
                "The iris is a pigmented muscular ring around the pupil.",
                "It constricts or dilates the pupil according to light intensity."
            )
        ),
        ScienceMcqItem(
            "CHEMISTRY", 2,
            "Which subatomic particle carries a unit negative electrical charge?",
            "Electron",
            listOf("Proton", "Neutron", "Electron", "Positron"),
            listOf(
                "Electrons orbit the atomic nucleus.",
                "Each electron carries a charge of −1.602 × 10⁻¹⁹ C."
            )
        ),
        ScienceMcqItem(
            "PHYSICS", 3,
            "What is the SI unit of electrical resistance?",
            "Ohm",
            listOf("Volt", "Ampere", "Ohm", "Watt"),
            listOf(
                "Electrical resistance opposes current flow in a conductor.",
                "By Ohm's law R = V / I, resistance is measured in Ohms (Ω)."
            )
        ),
        ScienceMcqItem(
            "BIOLOGY", 3,
            "Which organelle is the powerhouse of the eukaryotic cell because it synthesizes ATP?",
            "Mitochondrion",
            listOf("Ribosome", "Mitochondrion", "Golgi apparatus", "Lysosome"),
            listOf(
                "Aerobic cellular respiration takes place in mitochondria.",
                "Mitochondria generate ATP for cellular activities."
            )
        ),
        ScienceMcqItem(
            "PHYSICS", 4,
            "Which law states that net force equals mass multiplied by acceleration (F = m·a)?",
            "Newton's second law",
            listOf("Newton's first law", "Newton's second law", "Newton's third law", "Hooke's law"),
            listOf(
                "Rate of change of momentum is proportional to net applied force.",
                "For constant mass, F = m · a."
            )
        )
    )

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val tier = config.complexityTier.coerceIn(1, 4)
        val pool = bank.filter { it.minDifficulty <= difficulty.level }.ifEmpty { bank }
        val item = pool[random.nextInt(pool.size)]
        val shuffled = item.options.shuffled(random)
        return MathQuestion(
            id = UUID.randomUUID().toString(),
            category = category,
            difficulty = difficulty,
            answerFormat = AnswerFormat.MULTIPLE_CHOICE,
            prompt = item.prompt,
            answer = item.answer,
            explanationSteps = item.supportingFacts,
            explanation = item.supportingFacts.joinToString("\n"),
            options = shuffled,
            expectedTimeSeconds = 14,
            fingerprint = "SCI:${item.subject}:${item.prompt.hashCode()}",
            generatorVersion = version,
            metadata = QuestionMetadata.MultiStepMeta(item.prompt, item.answer),
            complexityTier = tier,
            questionStyle = config.questionStyle
        )
    }
}

/**
 * Part 7 — ECE Generator:
 * Network theory, Analog/Digital electronics, Signals & systems, Control,
 * Communication systems, Electromagnetics/Antennas, Microprocessors & VLSI.
 */
class EceEngineeringGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ENG_ECE
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val tier = config.complexityTier.coerceIn(1, 4)
        val style = config.questionStyle
        return when (random.nextInt(6)) {
            0 -> { // Signals & Systems: Nyquist sampling rate fs = 2 * fmax
                val fMaxKhz = random.nextInt(4, 25 + difficulty.level * 10)
                val nyquist = (2 * fMaxKhz).toLong()
                val prompt = "Signals: A band-limited signal has f_max = $fMaxKhz kHz. Minimum Nyquist sampling rate (kHz)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, nyquist,
                    listOf("Nyquist Theorem: f_s ≥ 2 · f_max", "f_s = 2 × $fMaxKhz = $nyquist kHz"),
                    14, "ECE:NYQ:$fMaxKhz", tier, style, random
                )
            }
            1 -> { // Analog Electronics: Non-inverting Op-Amp gain Av = 1 + Rf / R1
                val r1 = random.nextInt(2, 10)
                val ratio = random.nextInt(2, 6 + difficulty.level * 2)
                val rf = r1 * ratio
                val gain = (1 + ratio).toLong()
                val prompt = "Analog Op-Amp: Non-inverting amplifier has R₁ = ${r1}kΩ, R_f = ${rf}kΩ. Voltage gain A_v = ?"
                buildEngIntQuestion(
                    category, difficulty, prompt, gain,
                    listOf("A_v = 1 + (R_f / R₁)", "A_v = 1 + ($rf / $r1) = 1 + $ratio = $gain"),
                    16, "ECE:OPAMP:$r1:$rf", tier, style, random
                )
            }
            2 -> { // Digital / VLSI: N-bit ADC discrete quantization levels = 2^N
                val bits = random.nextInt(4, (6 + difficulty.level).coerceAtMost(12))
                val levels = 1L shl bits
                val prompt = "Digital / VLSI: How many discrete quantization levels does a $bits-bit ADC have?"
                buildEngIntQuestion(
                    category, difficulty, prompt, levels,
                    listOf("Levels L = 2^N", "L = 2^$bits = $levels"),
                    12, "ECE:ADC:$bits", tier, style, random
                )
            }
            3 -> { // Antennas & Electromagnetics: Free-space wavelength lambda = c / f
                val freqMhz = listOf(30, 50, 60, 100, 150, 300).random(random)
                val lambdaMeters = (300 / freqMhz).toLong()
                val prompt = "EM & Antennas: Find free-space wavelength λ (in meters) at f = $freqMhz MHz (c = 3×10⁸ m/s)."
                buildEngIntQuestion(
                    category, difficulty, prompt, lambdaMeters,
                    listOf("λ = c / f = 300 / f(MHz)", "λ = 300 ÷ $freqMhz = $lambdaMeters m"),
                    14, "ECE:WAV:$freqMhz", tier, style, random
                )
            }
            4 -> { // Microprocessors: Addressable memory in KB for N address lines
                val addrLines = random.nextInt(12, 17)
                val kb = 1L shl (addrLines - 10)
                val prompt = "Microprocessors: A CPU has $addrLines address lines (byte-addressable). Max memory in KB?"
                buildEngIntQuestion(
                    category, difficulty, prompt, kb,
                    listOf("2^10 bytes = 1 KB", "Memory = 2^($addrLines − 10) = 2^${addrLines - 10} = $kb KB"),
                    15, "ECE:MP:$addrLines", tier, style, random
                )
            }
            else -> { // Network Theory: Series-parallel equivalent resistance
                val r = listOf(4, 6, 8, 10, 12, 16, 20).random(random)
                val rSeries = random.nextInt(3, 15)
                val req = (r / 2 + rSeries).toLong()
                val prompt = "Network Theory: Two ${r}Ω resistors in parallel are in series with ${rSeries}Ω. Total R_eq (Ω)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, req,
                    listOf("${r}Ω ∥ ${r}Ω = ${r / 2}Ω", "R_eq = ${r / 2} + $rSeries = $req Ω"),
                    15, "ECE:NET:$r:$rSeries", tier, style, random
                )
            }
        }
    }
}

/**
 * Part 7 — EEE Generator:
 * Electrical machines, Power systems, Power electronics, Measurements & Circuit theory.
 */
class EeeEngineeringGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ENG_EEE
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val tier = config.complexityTier.coerceIn(1, 4)
        val style = config.questionStyle
        return when (random.nextInt(5)) {
            0 -> { // Electrical Machines: Synchronous speed Ns = 120 * f / P
                val freq = listOf(50, 60).random(random)
                val poles = listOf(2, 4, 6, 8, 12).random(random)
                val ns = (120L * freq) / poles
                val prompt = "Electrical Machines: Find synchronous speed N_s (RPM) of a $poles-pole AC motor at $freq Hz."
                buildEngIntQuestion(
                    category, difficulty, prompt, ns,
                    listOf("N_s = (120 × f) / P", "N_s = (120 × $freq) ÷ $poles = $ns RPM"),
                    14, "EEE:SYNC:$freq:$poles", tier, style, random
                )
            }
            1 -> { // Power Electronics: Step-down Buck converter V_out = D * V_in
                val dutyPct = listOf(20, 25, 40, 50, 60, 75, 80).random(random)
                val vIn = random.nextInt(2, 10) * 20
                val vOut = (vIn * dutyPct / 100).toLong()
                val prompt = "Power Electronics: Ideal DC-DC Buck converter has V_in = ${vIn}V and duty cycle D = $dutyPct%. V_out (V)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, vOut,
                    listOf("V_out = D × V_in", "V_out = ($dutyPct / 100) × $vIn = ${vOut}V"),
                    14, "EEE:BUCK:$vIn:$dutyPct", tier, style, random
                )
            }
            2 -> { // Transformers: Secondary voltage V2 = V1 * (N2 / N1)
                val n1 = random.nextInt(2, 8) * 100
                val ratio = random.nextInt(2, 6)
                val n2 = n1 / ratio
                val v1 = ratio * random.nextInt(40, 110)
                val v2 = (v1 / ratio).toLong()
                val prompt = "Transformers: N₁ = $n1 turns, N₂ = $n2 turns, primary V₁ = ${v1}V. Secondary V₂ (V)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, v2,
                    listOf("V₂ / V₁ = N₂ / N₁", "V₂ = $v1 × ($n2 / $n1) = ${v2}V"),
                    15, "EEE:TX:$n1:$n2:$v1", tier, style, random
                )
            }
            3 -> { // Circuit Theory & Power Systems: Joule heating power P = I^2 * R
                val current = random.nextInt(2, 6 + difficulty.level)
                val resistance = random.nextInt(3, 12 + difficulty.level * 2)
                val power = (current * current * resistance).toLong()
                val prompt = "Power Systems: A feeder line of R = ${resistance}Ω carries I = ${current}A. Real power loss I²R (Watts)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, power,
                    listOf("P = I² × R", "P = ${current}² × $resistance = ${current * current} × $resistance = $power W"),
                    14, "EEE:PWR:$current:$resistance", tier, style, random
                )
            }
            else -> { // Measurements: Energy meter revolutions = E(kWh) * K
                val kw = random.nextInt(2, 8)
                val hours = random.nextInt(2, 6)
                val meterConst = random.nextInt(1, 5) * 100
                val revs = (kw * hours * meterConst).toLong()
                val prompt = "Measurements: Load = ${kw}kW for ${hours}h. Meter constant = $meterConst rev/kWh. Total revolutions?"
                buildEngIntQuestion(
                    category, difficulty, prompt, revs,
                    listOf("Energy E = $kw × $hours = ${kw * hours} kWh", "Revolutions = ${kw * hours} × $meterConst = $revs"),
                    15, "EEE:MTR:$kw:$hours:$meterConst", tier, style, random
                )
            }
        }
    }
}

/**
 * Part 7 — CSE Generator:
 * Programming, Data structures, Algorithms, Operating systems, DBMS,
 * Computer networks & Computer architecture.
 */
class CseEngineeringGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ENG_CSE
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val tier = config.complexityTier.coerceIn(1, 4)
        val style = config.questionStyle
        return when (random.nextInt(5)) {
            0 -> { // Data Structures: Max nodes in a binary tree of height h (root at height 1) = 2^h - 1
                val h = random.nextInt(3, (5 + difficulty.level).coerceAtMost(9))
                val maxNodes = (1L shl h) - 1L
                val prompt = "Data Structures: Maximum nodes in a binary tree of height h = $h (root height = 1)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, maxNodes,
                    listOf("Max nodes = 2^h − 1", "= 2^$h − 1 = ${1L shl h} − 1 = $maxNodes"),
                    14, "CSE:TREE:$h", tier, style, random
                )
            }
            1 -> { // Computer Networks: Usable IPv4 hosts in /prefix subnet = 2^(32 - prefix) - 2
                val hostBits = random.nextInt(3, (5 + difficulty.level).coerceAtMost(9))
                val prefix = 32 - hostBits
                val usableHosts = (1L shl hostBits) - 2L
                val prompt = "Computer Networks: How many usable host IPv4 addresses exist in a /$prefix subnet?"
                buildEngIntQuestion(
                    category, difficulty, prompt, usableHosts,
                    listOf("Host bits = 32 − $prefix = $hostBits", "Usable hosts = 2^$hostBits − 2 = $usableHosts"),
                    16, "CSE:CIDR:$prefix", tier, style, random
                )
            }
            2 -> { // Operating Systems: Page table entries = Virtual Address Space / Page Size
                val vaBits = random.nextInt(20, 25)
                val pageBits = 12 // 4 KB page
                val pages = 1L shl (vaBits - pageBits)
                val prompt = "Operating Systems: A system has $vaBits-bit virtual addresses and 4 KB (2¹² B) pages. Total pages?"
                buildEngIntQuestion(
                    category, difficulty, prompt, pages,
                    listOf("Pages = 2^$vaBits / 2^12 = 2^${vaBits - 12}", "= $pages pages"),
                    15, "CSE:OS:$vaBits", tier, style, random
                )
            }
            3 -> { // DBMS: Blocking factor = floor(BlockSize / RecordSize)
                val recordSize = listOf(64, 100, 128, 200, 256).random(random)
                val blockSize = recordSize * random.nextInt(8, 20 + difficulty.level * 4)
                val bfr = (blockSize / recordSize).toLong()
                val prompt = "DBMS: Disk block size = $blockSize bytes, fixed record size = $recordSize bytes. Blocking factor?"
                buildEngIntQuestion(
                    category, difficulty, prompt, bfr,
                    listOf("bfr = ⌊BlockSize ÷ RecordSize⌋", "= $blockSize ÷ $recordSize = $bfr records/block"),
                    14, "CSE:DBMS:$blockSize:$recordSize", tier, style, random
                )
            }
            else -> { // Computer Architecture: Average Memory Access Time (AMAT)
                val hitTimeNs = random.nextInt(2, 8)
                val missRatePct = listOf(5, 10, 20).random(random)
                val missPenaltyNs = listOf(40, 60, 80, 100).random(random)
                val amat = (hitTimeNs + (missRatePct * missPenaltyNs) / 100).toLong()
                val prompt = "Computer Architecture: Cache hit = ${hitTimeNs}ns, miss rate = $missRatePct%, miss penalty = ${missPenaltyNs}ns. AMAT (ns)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, amat,
                    listOf("AMAT = HitTime + MissRate × MissPenalty", "= $hitTimeNs + ($missRatePct% × $missPenaltyNs) = $amat ns"),
                    16, "CSE:AMAT:$hitTimeNs:$missRatePct:$missPenaltyNs", tier, style, random
                )
            }
        }
    }
}

/**
 * Part 7 — Mechanical Engineering Generator:
 * Engineering mechanics, Thermodynamics, Fluid mechanics, Manufacturing,
 * Machine design & Heat transfer.
 */
class MechanicalEngineeringGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ENG_MECHANICAL
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val tier = config.complexityTier.coerceIn(1, 4)
        val style = config.questionStyle
        return when (random.nextInt(5)) {
            0 -> { // Thermodynamics: Carnot efficiency % = (1 - TL / TH) * 100
                val effPct = listOf(25, 40, 50, 60, 75).random(random)
                val tHigh = listOf(400, 500, 600, 800, 1000).random(random)
                val tLow = tHigh * (100 - effPct) / 100
                val prompt = "Thermodynamics: Carnot heat engine operates between T_H = ${tHigh}K and T_L = ${tLow}K. Efficiency (%)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, effPct.toLong(),
                    listOf("η = (1 − T_L / T_H) × 100%", "η = (1 − $tLow / $tHigh) × 100 = $effPct%"),
                    15, "MEC:CARNOT:$tHigh:$tLow", tier, style, random
                )
            }
            1 -> { // Fluid Mechanics: Continuity equation A1*V1 = A2*V2
                val a2 = random.nextInt(2, 8)
                val ratio = random.nextInt(2, 5)
                val a1 = a2 * ratio
                val v1 = random.nextInt(2, 8)
                val v2 = (v1 * ratio).toLong()
                val prompt = "Fluid Mechanics: Pipe area narrows from A₁ = ${a1}cm² (V₁ = ${v1}m/s) to A₂ = ${a2}cm². Exit velocity V₂ (m/s)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, v2,
                    listOf("Continuity: A₁V₁ = A₂V₂", "V₂ = ($a1 × $v1) ÷ $a2 = $v2 m/s"),
                    15, "MEC:CONT:$a1:$a2:$v1", tier, style, random
                )
            }
            2 -> { // Machine Design / Gear Train: Driven speed N2 = N1 * (T1 / T2)
                val t1 = random.nextInt(15, 30)
                val ratio = random.nextInt(2, 5)
                val t2 = t1 * ratio
                val n2 = random.nextInt(100, 350)
                val n1 = n2 * ratio
                val prompt = "Machine Design: Pinion has T₁ = $t1 teeth at N₁ = $n1 RPM driving gear with T₂ = $t2 teeth. Gear speed N₂ (RPM)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, n2.toLong(),
                    listOf("N₂ / N₁ = T₁ / T₂", "N₂ = $n1 × ($t1 / $t2) = $n2 RPM"),
                    15, "MEC:GEAR:$t1:$t2:$n1", tier, style, random
                )
            }
            3 -> { // Heat Transfer: Fourier's 1D conduction Q = k * A * dT / L
                val k = random.nextInt(2, 10)
                val area = random.nextInt(2, 6)
                val length = random.nextInt(1, 4)
                val dT = length * random.nextInt(15, 45)
                val q = (k * area * dT / length).toLong()
                val prompt = "Heat Transfer: Wall has k = $k W/mK, A = ${area}m², L = ${length}m, ΔT = ${dT}K. Heat rate Q (W)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, q,
                    listOf("Fourier Law: Q = k · A · ΔT / L", "Q = ($k × $area × $dT) ÷ $length = $q W"),
                    16, "MEC:HEAT:$k:$area:$dT:$length", tier, style, random
                )
            }
            else -> { // Engineering Mechanics: Axial stress sigma = F / A
                val areaMm2 = random.nextInt(10, 40)
                val stressMpa = random.nextInt(15, 80)
                val forceN = areaMm2 * stressMpa
                val prompt = "Engineering Mechanics: A rod of cross-section A = ${areaMm2}mm² carries axial load F = ${forceN}N. Stress σ (MPa)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, stressMpa.toLong(),
                    listOf("σ = F / A (1 N/mm² = 1 MPa)", "σ = $forceN ÷ $areaMm2 = $stressMpa MPa"),
                    14, "MEC:STR:$forceN:$areaMm2", tier, style, random
                )
            }
        }
    }
}

/**
 * Part 7 — Civil Engineering Generator:
 * Structural engineering, Surveying, Geotechnical engineering,
 * Transportation & Hydraulics.
 */
class CivilEngineeringGenerator : QuestionGenerator {
    override val category: MathCategory = MathCategory.ENG_CIVIL
    override val version: Int = 1

    override fun generate(
        difficulty: Difficulty,
        config: GeneratorConfig,
        random: Random
    ): MathQuestion {
        val tier = config.complexityTier.coerceIn(1, 4)
        val style = config.questionStyle
        return when (random.nextInt(5)) {
            0 -> { // Structural Engineering: Simply supported beam with central point load M_max = W * L / 4
                val span = random.nextInt(2, 6) * 2
                val load = random.nextInt(4, 16) * 2
                val mMax = (load * span / 4).toLong()
                val prompt = "Structural Eng: Simply supported beam of span L = ${span}m carries central load W = ${load}kN. Max bending moment (kN·m)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, mMax,
                    listOf("M_max = (W × L) / 4 at mid-span", "M_max = ($load × $span) ÷ 4 = $mMax kN·m"),
                    15, "CIV:BEAM:$load:$span", tier, style, random
                )
            }
            1 -> { // Surveying: Height of Instrument (HI = BM + BS) and Reduced Level (RL = HI - FS)
                val bm = random.nextInt(100, 250)
                val bs = random.nextInt(1, 4)
                val fs = random.nextInt(1, 4)
                val rl = (bm + bs - fs).toLong()
                val prompt = "Surveying: Benchmark RL = ${bm}m, Backsight BS = ${bs}m, Foresight FS = ${fs}m. Station RL (m)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, rl,
                    listOf("HI = BM + BS = $bm + $bs = ${bm + bs}m", "RL = HI − FS = ${bm + bs} − $fs = $rl m"),
                    14, "CIV:SURV:$bm:$bs:$fs", tier, style, random
                )
            }
            2 -> { // Geotechnical Engineering: Dry unit weight gamma_d = gamma_t / (1 + w)
                val waterPct = listOf(10, 20, 25).random(random)
                val dryUnit = listOf(15, 16, 18, 20).random(random)
                val bulkUnit = dryUnit * (100 + waterPct) / 100
                // Choose combinations where bulkUnit is exact integer
                val exactDry = 20
                val exactW = 20
                val exactBulk = 24
                val prompt = "Geotechnical: Bulk unit weight γ = ${exactBulk}kN/m³ at water content w = ${exactW}%. Dry unit weight γ_d (kN/m³)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, exactDry.toLong(),
                    listOf("γ_d = γ / (1 + w)", "γ_d = $exactBulk ÷ 1.20 = $exactDry kN/m³"),
                    15, "CIV:GEO:$bulkUnit:$waterPct", tier, style, random
                )
            }
            3 -> { // Hydraulics: Open channel discharge Q = A * V
                val width = random.nextInt(2, 7)
                val depth = random.nextInt(1, 5)
                val velocity = random.nextInt(2, 6)
                val q = (width * depth * velocity).toLong()
                val prompt = "Hydraulics: Rectangular channel (width = ${width}m, depth = ${depth}m) flows at V = ${velocity}m/s. Discharge Q (m³/s)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, q,
                    listOf("Area A = b × y = $width × $depth = ${width * depth} m²", "Q = A × V = ${width * depth} × $velocity = $q m³/s"),
                    14, "CIV:HYD:$width:$depth:$velocity", tier, style, random
                )
            }
            else -> { // Transportation Engineering: Stopping sight reaction distance d = v(m/s) * t
                val speedMs = random.nextInt(10, 30)
                val reactionSec = random.nextInt(2, 4)
                val lagDist = (speedMs * reactionSec).toLong()
                val prompt = "Transportation: Vehicle speed = ${speedMs}m/s, driver reaction time t_r = ${reactionSec}s. Lag distance (m)?"
                buildEngIntQuestion(
                    category, difficulty, prompt, lagDist,
                    listOf("Lag distance d_lag = v × t_r", "d_lag = $speedMs × $reactionSec = $lagDist m"),
                    12, "CIV:TRN:$speedMs:$reactionSec", tier, style, random
                )
            }
        }
    }
}
