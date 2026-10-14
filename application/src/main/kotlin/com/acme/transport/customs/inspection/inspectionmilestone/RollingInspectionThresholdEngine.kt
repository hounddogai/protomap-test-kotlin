package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.flags.Registry

/** One step of the rolling inspection threshold calculation. */
interface RollingInspectionThresholdModifier {
    fun describe(): String
    fun adjust(item: RollingInspectionThreshold, value: Double): Double
}

data class PercentRollingInspectionThresholdAdjuster(var percent: Double) : RollingInspectionThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingInspectionThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingInspectionThresholdAdjuster(var cap: Double) : RollingInspectionThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingInspectionThreshold, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingInspectionThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingInspectionThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingInspectionThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling inspection threshold steps in order and keeps a trace of each. */
data class RollingInspectionThresholdEngine(
    var steps: MutableList<RollingInspectionThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingInspectionThreshold): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingInspectionThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingInspectionThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingInspectionThresholdEngine(flags: Registry): RollingInspectionThresholdEngine {
    return RollingInspectionThresholdEngine(
        steps = mutableListOf<RollingInspectionThresholdModifier>(
            PercentRollingInspectionThresholdAdjuster(8.8),
            CapRollingInspectionThresholdAdjuster(21426.0),
            FlaggedRollingInspectionThresholdAdjuster(flags, "rolling-inspection-threshold-lenient", -3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 11746.8,
    )
}
