package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal dock door estimate calculation. */
interface SeasonalDockDoorEstimateAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalDockDoorEstimate, value: Double): Double
}

data class RegionSeasonalDockDoorEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDockDoorEstimateAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: SeasonalDockDoorEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSeasonalDockDoorEstimateAdjuster(var minimum: Double) : SeasonalDockDoorEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDockDoorEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentSeasonalDockDoorEstimateAdjuster(var percent: Double) : SeasonalDockDoorEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDockDoorEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalDockDoorEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDockDoorEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDockDoorEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalDockDoorEstimateScoreOffsetAdjuster(var weight: Double) : SeasonalDockDoorEstimateAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: SeasonalDockDoorEstimate, value: Double): Double = value + item.score * weight
}

/** Runs the seasonal dock door estimate steps in order and keeps a trace of each. */
data class SeasonalDockDoorEstimateEngine(
    var steps: MutableList<SeasonalDockDoorEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalDockDoorEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDockDoorEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDockDoorEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalDockDoorEstimateEngine(flags: Registry): SeasonalDockDoorEstimateEngine {
    return SeasonalDockDoorEstimateEngine(
        steps = mutableListOf<SeasonalDockDoorEstimateAdjuster>(
            RegionSeasonalDockDoorEstimateAdjuster(
                mutableMapOf<String, Double>("north" to 0.72, "retail" to 0.83, "central" to 0.76),
            ),
            MinimumSeasonalDockDoorEstimateAdjuster(13.7),
            PercentSeasonalDockDoorEstimateAdjuster(3.2),
            FlaggedSeasonalDockDoorEstimateAdjuster(flags, "seasonal-dock-door-estimate-tiered", -6.8),
            SeasonalDockDoorEstimateScoreOffsetAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 21615.0,
    )
}
