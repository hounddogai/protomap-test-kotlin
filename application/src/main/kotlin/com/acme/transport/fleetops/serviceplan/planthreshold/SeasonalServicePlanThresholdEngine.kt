package com.acme.transport.fleetops.serviceplan.planthreshold

/** One step of the seasonal service plan threshold calculation. */
interface SeasonalServicePlanThresholdStep {
    fun describe(): String
    fun adjust(item: SeasonalServicePlanThreshold, value: Double): Double
}

data class CapSeasonalServicePlanThresholdAdjuster(var cap: Double) : SeasonalServicePlanThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServicePlanThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalServicePlanThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalServicePlanThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalServicePlanThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentSeasonalServicePlanThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalServicePlanThresholdStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: SeasonalServicePlanThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal service plan threshold steps in order and keeps a trace of each. */
data class SeasonalServicePlanThresholdEngine(
    var steps: MutableList<SeasonalServicePlanThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalServicePlanThreshold): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalServicePlanThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalServicePlanThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalServicePlanThresholdEngine(): SeasonalServicePlanThresholdEngine {
    return SeasonalServicePlanThresholdEngine(
        steps = mutableListOf<SeasonalServicePlanThresholdStep>(
            CapSeasonalServicePlanThresholdAdjuster(7268.0),
            TieredSeasonalServicePlanThresholdAdjuster(
                mutableListOf<Double>(2618.0, 2695.0, 6419.0),
                mutableListOf<Double>(0.83, 0.95, 1.05),
            ),
            SegmentSeasonalServicePlanThresholdAdjuster(mutableMapOf<String, Double>("west" to 1.24)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 40471.3,
    )
}
