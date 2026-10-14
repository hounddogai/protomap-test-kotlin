package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback attempt policy calculation. */
interface FallbackAttemptPolicyModifier {
    fun describe(): String
    fun adjust(item: FallbackAttemptPolicy, value: Double): Double
}

data class PercentFallbackAttemptPolicyAdjuster(var percent: Double) : FallbackAttemptPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAttemptPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackAttemptPolicyAdjuster(var minimum: Double) : FallbackAttemptPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAttemptPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackAttemptPolicyAdjuster(var cap: Double) : FallbackAttemptPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAttemptPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackAttemptPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAttemptPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAttemptPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackAttemptPolicyAdjuster(var step: Double) : FallbackAttemptPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackAttemptPolicy, value: Double): Double = roundTo(value, step)
}

data class LocaleFallbackAttemptPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackAttemptPolicyModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackAttemptPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback attempt policy steps in order and keeps a trace of each. */
data class FallbackAttemptPolicyEngine(
    var steps: MutableList<FallbackAttemptPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackAttemptPolicy): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackAttemptPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackAttemptPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackAttemptPolicyEngine(): FallbackAttemptPolicyEngine {
    return FallbackAttemptPolicyEngine(
        steps = mutableListOf<FallbackAttemptPolicyModifier>(
            PercentFallbackAttemptPolicyAdjuster(23.2),
            MinimumFallbackAttemptPolicyAdjuster(15.6),
            CapFallbackAttemptPolicyAdjuster(33847.0),
            TieredFallbackAttemptPolicyAdjuster(
                mutableListOf<Double>(746.0, 986.0, 1723.0, 7242.0),
                mutableListOf<Double>(0.96, 0.83, 1.15, 0.87),
            ),
            RoundingFallbackAttemptPolicyAdjuster(1.0),
            LocaleFallbackAttemptPolicyAdjuster(mutableMapOf<String, Double>("west" to 1.01)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 91595.3,
    )
}
