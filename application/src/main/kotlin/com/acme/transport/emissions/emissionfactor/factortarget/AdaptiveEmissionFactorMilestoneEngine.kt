package com.acme.transport.emissions.emissionfactor.factortarget

/** One step of the adaptive emission factor milestone calculation. */
interface AdaptiveEmissionFactorMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double
}

data class CapAdaptiveEmissionFactorMilestoneAdjuster(
    var cap: Double,
) : AdaptiveEmissionFactorMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveEmissionFactorMilestoneAdjuster(
    var minimum: Double,
) : AdaptiveEmissionFactorMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveEmissionFactorMilestoneMarginOffsetAdjuster(
    var weight: Double,
) : AdaptiveEmissionFactorMilestoneAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double = value + item.margin * weight
}

data class TitleAdaptiveEmissionFactorMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveEmissionFactorMilestoneAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveEmissionFactorMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveEmissionFactorMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAdaptiveEmissionFactorMilestoneAdjuster(
    var percent: Double,
) : AdaptiveEmissionFactorMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveEmissionFactorMilestone, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive emission factor milestone steps in order and keeps a trace of each. */
data class AdaptiveEmissionFactorMilestoneEngine(
    var steps: MutableList<AdaptiveEmissionFactorMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveEmissionFactorMilestone): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEmissionFactorMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEmissionFactorMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveEmissionFactorMilestoneEngine(): AdaptiveEmissionFactorMilestoneEngine {
    return AdaptiveEmissionFactorMilestoneEngine(
        steps = mutableListOf<AdaptiveEmissionFactorMilestoneAdjuster>(
            CapAdaptiveEmissionFactorMilestoneAdjuster(39818.0),
            MinimumAdaptiveEmissionFactorMilestoneAdjuster(20.2),
            AdaptiveEmissionFactorMilestoneMarginOffsetAdjuster(0.18),
            TitleAdaptiveEmissionFactorMilestoneAdjuster(mutableMapOf<String, Double>("south" to 1.39)),
            TieredAdaptiveEmissionFactorMilestoneAdjuster(
                mutableListOf<Double>(235.0, 1277.0, 2764.0, 4554.0),
                mutableListOf<Double>(1.08, 0.98, 1.15, 1.18),
            ),
            PercentAdaptiveEmissionFactorMilestoneAdjuster(-19.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 18317.9,
    )
}
