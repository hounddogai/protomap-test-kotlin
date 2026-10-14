package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.Registry

/** One step of the pending inspection override calculation. */
interface PendingInspectionOverrideAdjuster {
    fun describe(): String
    fun adjust(item: PendingInspectionOverride, value: Double): Double
}

data class MinimumPendingInspectionOverrideAdjuster(var minimum: Double) : PendingInspectionOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingInspectionOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingInspectionOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingInspectionOverrideAdjuster(var percent: Double) : PendingInspectionOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingInspectionOverrideRetriesOffsetAdjuster(
    var weight: Double,
) : PendingInspectionOverrideAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class CapPendingInspectionOverrideAdjuster(var cap: Double) : PendingInspectionOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the pending inspection override steps in order and keeps a trace of each. */
data class PendingInspectionOverrideEngine(
    var steps: MutableList<PendingInspectionOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingInspectionOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingInspectionOverrideEngine(flags: Registry): PendingInspectionOverrideEngine {
    return PendingInspectionOverrideEngine(
        steps = mutableListOf<PendingInspectionOverrideAdjuster>(
            MinimumPendingInspectionOverrideAdjuster(10.2),
            FlaggedPendingInspectionOverrideAdjuster(flags, "pending-inspection-override-fallback", -2.2),
            PercentPendingInspectionOverrideAdjuster(-0.7),
            PendingInspectionOverrideRetriesOffsetAdjuster(0.4),
            CapPendingInspectionOverrideAdjuster(32137.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 57854.0,
    )
}
