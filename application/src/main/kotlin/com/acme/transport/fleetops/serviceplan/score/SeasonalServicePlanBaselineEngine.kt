package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal service plan baseline calculation. */
interface SeasonalServicePlanBaselineFactor {
    fun describe(): String
    fun adjust(item: SeasonalServicePlanBaseline, value: Double): Double
}

data class SeasonalServicePlanBaselineWeightOffsetAdjuster(
    var weight: Double,
) : SeasonalServicePlanBaselineFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: SeasonalServicePlanBaseline, value: Double): Double = value + item.weight * weight
}

data class PercentSeasonalServicePlanBaselineAdjuster(var percent: Double) : SeasonalServicePlanBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalServicePlanBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalServicePlanBaselineAdjuster(var cap: Double) : SeasonalServicePlanBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServicePlanBaseline, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalServicePlanBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalServicePlanBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalServicePlanBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal service plan baseline steps in order and keeps a trace of each. */
data class SeasonalServicePlanBaselineEngine(
    var steps: MutableList<SeasonalServicePlanBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalServicePlanBaseline): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalServicePlanBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalServicePlanBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalServicePlanBaselineEngine(flags: Registry): SeasonalServicePlanBaselineEngine {
    return SeasonalServicePlanBaselineEngine(
        steps = mutableListOf<SeasonalServicePlanBaselineFactor>(
            SeasonalServicePlanBaselineWeightOffsetAdjuster(1.13),
            PercentSeasonalServicePlanBaselineAdjuster(12.3),
            CapSeasonalServicePlanBaselineAdjuster(28729.0),
            FlaggedSeasonalServicePlanBaselineAdjuster(flags, "seasonal-service-plan-baseline-primary", -8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 14797.8,
    )
}
