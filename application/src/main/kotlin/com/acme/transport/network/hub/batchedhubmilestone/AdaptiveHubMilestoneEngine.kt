package com.acme.transport.network.hub.batchedhubmilestone

/** One step of the adaptive hub milestone calculation. */
interface AdaptiveHubMilestoneFactor {
    fun describe(): String
    fun adjust(item: AdaptiveHubMilestone, value: Double): Double
}

data class CapAdaptiveHubMilestoneAdjuster(var cap: Double) : AdaptiveHubMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveHubMilestone, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveHubMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveHubMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveHubMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveHubMilestoneLimitOffsetAdjuster(var weight: Double) : AdaptiveHubMilestoneFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: AdaptiveHubMilestone, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentAdaptiveHubMilestoneAdjuster(var percent: Double) : AdaptiveHubMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveHubMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotAdaptiveHubMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveHubMilestoneFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: AdaptiveHubMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveHubMilestoneAdjuster(var minimum: Double) : AdaptiveHubMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveHubMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive hub milestone steps in order and keeps a trace of each. */
data class AdaptiveHubMilestoneEngine(
    var steps: MutableList<AdaptiveHubMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveHubMilestone): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveHubMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveHubMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveHubMilestoneEngine(): AdaptiveHubMilestoneEngine {
    return AdaptiveHubMilestoneEngine(
        steps = mutableListOf<AdaptiveHubMilestoneFactor>(
            CapAdaptiveHubMilestoneAdjuster(5560.0),
            TieredAdaptiveHubMilestoneAdjuster(
                mutableListOf<Double>(144.0, 9667.0),
                mutableListOf<Double>(0.82, 1.15),
            ),
            AdaptiveHubMilestoneLimitOffsetAdjuster(0.51),
            PercentAdaptiveHubMilestoneAdjuster(13.5),
            DepotAdaptiveHubMilestoneAdjuster(mutableMapOf<String, Double>("south" to 0.87)),
            MinimumAdaptiveHubMilestoneAdjuster(43.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 61688.1,
    )
}
