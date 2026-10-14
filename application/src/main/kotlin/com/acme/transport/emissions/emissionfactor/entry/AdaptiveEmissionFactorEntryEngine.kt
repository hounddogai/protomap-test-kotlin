package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive emission factor entry calculation. */
interface AdaptiveEmissionFactorEntryAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveEmissionFactorEntry, value: Double): Double
}

data class MinimumAdaptiveEmissionFactorEntryAdjuster(
    var minimum: Double,
) : AdaptiveEmissionFactorEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveEmissionFactorEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleAdaptiveEmissionFactorEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveEmissionFactorEntryAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: AdaptiveEmissionFactorEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveEmissionFactorEntryAdjuster(var step: Double) : AdaptiveEmissionFactorEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveEmissionFactorEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive emission factor entry steps in order and keeps a trace of each. */
data class AdaptiveEmissionFactorEntryEngine(
    var steps: MutableList<AdaptiveEmissionFactorEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveEmissionFactorEntry): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEmissionFactorEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEmissionFactorEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveEmissionFactorEntryEngine(): AdaptiveEmissionFactorEntryEngine {
    return AdaptiveEmissionFactorEntryEngine(
        steps = mutableListOf<AdaptiveEmissionFactorEntryAdjuster>(
            MinimumAdaptiveEmissionFactorEntryAdjuster(42.4),
            LocaleAdaptiveEmissionFactorEntryAdjuster(
                mutableMapOf<String, Double>("west" to 1.33, "south" to 0.83, "online" to 1.13),
            ),
            RoundingAdaptiveEmissionFactorEntryAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 47637.1,
    )
}
