package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.flags.Registry

/** One step of the pending defect threshold calculation. */
interface PendingDefectThresholdModifier {
    fun describe(): String
    fun adjust(item: PendingDefectThreshold, value: Double): Double
}

data class CapPendingDefectThresholdAdjuster(var cap: Double) : PendingDefectThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDefectThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentPendingDefectThresholdAdjuster(var percent: Double) : PendingDefectThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDefectThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingDefectThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDefectThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDefectThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending defect threshold steps in order and keeps a trace of each. */
data class PendingDefectThresholdEngine(
    var steps: MutableList<PendingDefectThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingDefectThreshold): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDefectThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDefectThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingDefectThresholdEngine(flags: Registry): PendingDefectThresholdEngine {
    return PendingDefectThresholdEngine(
        steps = mutableListOf<PendingDefectThresholdModifier>(
            CapPendingDefectThresholdAdjuster(10261.0),
            PercentPendingDefectThresholdAdjuster(-14.3),
            FlaggedPendingDefectThresholdAdjuster(flags, "pending-defect-threshold-pending", -5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 44430.7,
    )
}
