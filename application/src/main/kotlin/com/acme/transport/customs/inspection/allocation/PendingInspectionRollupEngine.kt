package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the pending inspection rollup calculation. */
interface PendingInspectionRollupAdjuster {
    fun describe(): String
    fun adjust(item: PendingInspectionRollup, value: Double): Double
}

data class FlaggedPendingInspectionRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingInspectionRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingInspectionRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelPendingInspectionRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionRollupAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingInspectionRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingInspectionRollupAdjuster(var percent: Double) : PendingInspectionRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending inspection rollup steps in order and keeps a trace of each. */
data class PendingInspectionRollupEngine(
    var steps: MutableList<PendingInspectionRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingInspectionRollup): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingInspectionRollupEngine(flags: Registry): PendingInspectionRollupEngine {
    return PendingInspectionRollupEngine(
        steps = mutableListOf<PendingInspectionRollupAdjuster>(
            FlaggedPendingInspectionRollupAdjuster(flags, "pending-inspection-rollup-regional", -0.5),
            LabelPendingInspectionRollupAdjuster(mutableMapOf<String, Double>("east" to 0.99, "retail" to 0.95)),
            PercentPendingInspectionRollupAdjuster(-10.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 13368.0,
    )
}
