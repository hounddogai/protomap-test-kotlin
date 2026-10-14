package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.flags.Registry

/** One step of the rolling utilization estimate calculation. */
interface RollingUtilizationEstimateStep {
    fun describe(): String
    fun adjust(item: RollingUtilizationEstimate, value: Double): Double
}

data class FlaggedRollingUtilizationEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingUtilizationEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingUtilizationEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingUtilizationEstimateAdjuster(var percent: Double) : RollingUtilizationEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingUtilizationEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameRollingUtilizationEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingUtilizationEstimateStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingUtilizationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling utilization estimate steps in order and keeps a trace of each. */
data class RollingUtilizationEstimateEngine(
    var steps: MutableList<RollingUtilizationEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingUtilizationEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingUtilizationEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingUtilizationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingUtilizationEstimateEngine(flags: Registry): RollingUtilizationEstimateEngine {
    return RollingUtilizationEstimateEngine(
        steps = mutableListOf<RollingUtilizationEstimateStep>(
            FlaggedRollingUtilizationEstimateAdjuster(flags, "rolling-utilization-estimate-regional", 7.5),
            PercentRollingUtilizationEstimateAdjuster(7.0),
            NameRollingUtilizationEstimateAdjuster(mutableMapOf<String, Double>("east" to 0.79, "north" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 90179.0,
    )
}
