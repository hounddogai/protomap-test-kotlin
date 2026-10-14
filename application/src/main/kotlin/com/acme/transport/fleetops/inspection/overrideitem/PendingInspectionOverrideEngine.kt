package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the pending inspection override calculation. */
interface PendingInspectionOverrideModifier {
    fun describe(): String
    fun adjust(item: PendingInspectionOverride, value: Double): Double
}

data class TitlePendingInspectionOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionOverrideModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingInspectionOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingInspectionOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingInspectionOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingInspectionOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingInspectionOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending inspection override steps in order and keeps a trace of each. */
data class PendingInspectionOverrideEngine(
    var steps: MutableList<PendingInspectionOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingInspectionOverride): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionOverrideModifier = steps[index]
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
        steps = mutableListOf<PendingInspectionOverrideModifier>(
            TitlePendingInspectionOverrideAdjuster(mutableMapOf<String, Double>("south" to 1.11)),
            FlaggedPendingInspectionOverrideAdjuster(flags, "pending-inspection-override-strict", -9.2),
            TieredPendingInspectionOverrideAdjuster(
                mutableListOf<Double>(671.0, 6956.0, 7919.0),
                mutableListOf<Double>(1.06, 1.07, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 42770.4,
    )
}
