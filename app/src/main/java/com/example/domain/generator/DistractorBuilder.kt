package com.example.domain.generator

import com.example.domain.model.Fraction
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.abs
import kotlin.random.Random

object DistractorBuilder {

    fun integerOptions(
        correct: Long,
        allowNegative: Boolean,
        random: Random
    ): List<String> {
        val candidates = linkedSetOf<Long>()
        val magnitude = abs(correct).coerceAtLeast(1L)

        // Domain-plausible offsets: off-by-1, carrying off-by-10, digit swap / scaling errors
        val baseOffsets = buildList {
            add(1L)
            add(-1L)
            add(2L)
            add(-2L)
            add(10L)
            add(-10L)
            if (magnitude >= 50L) {
                add(20L)
                add(-20L)
                add(100L)
                add(-100L)
            }
            if (magnitude >= 500L) {
                add(1000L)
                add(-1000L)
            }
        }.shuffled(random)

        for (offset in baseOffsets) {
            if (candidates.size >= 3) break
            val candidate = correct + offset
            if (candidate != correct && (allowNegative || candidate >= 0L)) {
                candidates.add(candidate)
            }
        }

        // Fallback guaranteed unique distractors
        var delta = 3L
        while (candidates.size < 3) {
            val plusCandidate = correct + delta
            if (plusCandidate != correct && (allowNegative || plusCandidate >= 0L)) {
                candidates.add(plusCandidate)
            }
            if (candidates.size < 3) {
                val minusCandidate = correct - delta
                if (minusCandidate != correct && (allowNegative || minusCandidate >= 0L)) {
                    candidates.add(minusCandidate)
                }
            }
            delta++
        }

        val all = (candidates.take(3).map { it.toString() } + correct.toString()).distinct()
        return all.shuffled(random)
    }

    fun decimalOptions(
        correct: BigDecimal,
        allowNegative: Boolean,
        random: Random
    ): List<String> {
        val normalizedCorrect = formatDecimal(correct)
        val scale = correct.stripTrailingZeros().scale().coerceAtLeast(1)
        val ulp = BigDecimal.ONE.movePointLeft(scale)
        val tenUlp = ulp.multiply(BigDecimal.TEN)

        val candidates = linkedSetOf<String>()
        val rawMutations = listOf(
            correct.add(ulp),
            correct.subtract(ulp),
            correct.add(tenUlp),
            correct.subtract(tenUlp),
            correct.movePointRight(1),
            correct.movePointLeft(1),
            correct.add(ulp.multiply(BigDecimal(2))),
            correct.subtract(ulp.multiply(BigDecimal(2)))
        ).shuffled(random)

        for (candidate in rawMutations) {
            if (candidates.size >= 3) break
            if (!allowNegative && candidate < BigDecimal.ZERO) continue
            val formatted = formatDecimal(candidate)
            if (formatted != normalizedCorrect) {
                candidates.add(formatted)
            }
        }

        var step = 3
        while (candidates.size < 3) {
            val candidate = correct.add(ulp.multiply(BigDecimal(step)))
            val formatted = formatDecimal(candidate)
            if (formatted != normalizedCorrect) {
                candidates.add(formatted)
            }
            step++
        }

        return (candidates.take(3) + normalizedCorrect).shuffled(random)
    }

    fun fractionOptions(
        correct: Fraction,
        random: Random
    ): List<String> {
        val normCorrect = correct.normalized()
        val correctStr = normCorrect.toCanonicalString()
        val candidates = linkedSetOf<String>()

        val num = normCorrect.numerator
        val den = normCorrect.denominator

        val perturbations = listOf(
            Fraction(num + BigInteger.ONE, den),
            Fraction((num - BigInteger.ONE).let { if (it == BigInteger.ZERO) num + BigInteger.valueOf(2) else it }, den),
            Fraction(num, den + BigInteger.ONE),
            if (den > BigInteger.TWO) Fraction(num, den - BigInteger.ONE) else Fraction(num + BigInteger.TWO, den + BigInteger.TWO),
            Fraction(num + BigInteger.ONE, den + BigInteger.ONE),
            Fraction(den, if (num != BigInteger.ZERO) num.abs() else BigInteger.ONE)
        ).shuffled(random)

        for (p in perturbations) {
            if (candidates.size >= 3) break
            val pStr = p.toCanonicalString()
            if (pStr != correctStr) {
                candidates.add(pStr)
            }
        }

        var offset = 2L
        while (candidates.size < 3) {
            val fallback = Fraction(num + BigInteger.valueOf(offset), den + BigInteger.ONE)
            val fStr = fallback.toCanonicalString()
            if (fStr != correctStr) {
                candidates.add(fStr)
            }
            offset++
        }

        return (candidates.take(3) + correctStr).shuffled(random)
    }

    fun formatDecimal(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        return if (stripped.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            stripped.toPlainString()
        }
    }
}
