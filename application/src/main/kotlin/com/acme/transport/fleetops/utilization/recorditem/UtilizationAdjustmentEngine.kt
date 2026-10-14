package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the utilization adjustment calculation. */
interface UtilizationAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: UtilizationAdjustment, value: Double): Double
}

data class CapUtilizationAdjustmentAdjuster(var cap: Double) : UtilizationAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationAdjustment, value: Double): Double = minOf(value, cap)
}

data class MinimumUtilizationAdjustmentAdjuster(var minimum: Double) : UtilizationAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedUtilizationAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the utilization adjustment steps in order and keeps a trace of each. */
data class UtilizationAdjustmentEngine(
    var steps: MutableList<UtilizationAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: UtilizationAdjustment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultUtilizationAdjustmentEngine(flags: Registry): UtilizationAdjustmentEngine {
    return UtilizationAdjustmentEngine(
        steps = mutableListOf<UtilizationAdjustmentAdjuster>(
            CapUtilizationAdjustmentAdjuster(25928.0),
            MinimumUtilizationAdjustmentAdjuster(40.8),
            FlaggedUtilizationAdjustmentAdjuster(flags, "utilization-adjustment-capped", -8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 83438.6,
    )
}
