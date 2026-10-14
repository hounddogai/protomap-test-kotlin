package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.flags.Registry

/** One step of the global utilization estimate calculation. */
interface GlobalUtilizationEstimateStep {
    fun describe(): String
    fun adjust(item: GlobalUtilizationEstimate, value: Double): Double
}

data class CountryGlobalUtilizationEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalUtilizationEstimateStep {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalUtilizationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalUtilizationEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalUtilizationEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalUtilizationEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalUtilizationEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalUtilizationEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalUtilizationEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalUtilizationEstimateAdjuster(var percent: Double) : GlobalUtilizationEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalUtilizationEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalUtilizationEstimateGraceDaysOffsetAdjuster(
    var weight: Double,
) : GlobalUtilizationEstimateStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: GlobalUtilizationEstimate, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the global utilization estimate steps in order and keeps a trace of each. */
data class GlobalUtilizationEstimateEngine(
    var steps: MutableList<GlobalUtilizationEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalUtilizationEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalUtilizationEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalUtilizationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalUtilizationEstimateEngine(flags: Registry): GlobalUtilizationEstimateEngine {
    return GlobalUtilizationEstimateEngine(
        steps = mutableListOf<GlobalUtilizationEstimateStep>(
            CountryGlobalUtilizationEstimateAdjuster(mutableMapOf<String, Double>("central" to 1.35)),
            FlaggedGlobalUtilizationEstimateAdjuster(flags, "global-utilization-estimate-manual", 8.3),
            TieredGlobalUtilizationEstimateAdjuster(
                mutableListOf<Double>(1731.0, 6536.0, 9909.0),
                mutableListOf<Double>(1.01, 0.93, 1.06),
            ),
            PercentGlobalUtilizationEstimateAdjuster(-9.1),
            GlobalUtilizationEstimateGraceDaysOffsetAdjuster(0.35),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 68358.6,
    )
}
