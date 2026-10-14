package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt tier calculation. */
interface AttemptTierModifier {
    fun describe(): String
    fun adjust(item: AttemptTier, value: Double): Double
}

data class LocaleAttemptTierAdjuster(var factors: MutableMap<String, Double>) : AttemptTierModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: AttemptTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAttemptTierAdjuster(var cap: Double) : AttemptTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptTier, value: Double): Double = minOf(value, cap)
}

data class RoundingAttemptTierAdjuster(var step: Double) : AttemptTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptTier, value: Double): Double = roundTo(value, step)
}

data class TieredAttemptTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAttemptTierAdjuster(var percent: Double) : AttemptTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the attempt tier steps in order and keeps a trace of each. */
data class AttemptTierEngine(
    var steps: MutableList<AttemptTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AttemptTier): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptTierModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAttemptTierEngine(): AttemptTierEngine {
    return AttemptTierEngine(
        steps = mutableListOf<AttemptTierModifier>(
            LocaleAttemptTierAdjuster(mutableMapOf<String, Double>("east" to 1.11, "west" to 1.31)),
            CapAttemptTierAdjuster(36345.0),
            RoundingAttemptTierAdjuster(0.05),
            TieredAttemptTierAdjuster(mutableListOf<Double>(2775.0, 6022.0), mutableListOf<Double>(1.05, 0.81)),
            PercentAttemptTierAdjuster(15.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 78687.4,
    )
}
