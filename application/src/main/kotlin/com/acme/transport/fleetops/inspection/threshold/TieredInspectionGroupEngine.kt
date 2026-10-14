package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the tiered inspection group calculation. */
interface TieredInspectionGroupAdjuster {
    fun describe(): String
    fun adjust(item: TieredInspectionGroup, value: Double): Double
}

data class FlaggedTieredInspectionGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredInspectionGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredInspectionGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredInspectionGroupAdjuster(var minimum: Double) : TieredInspectionGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredInspectionGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredInspectionGroupDurationMinutesOffsetAdjuster(
    var weight: Double,
) : TieredInspectionGroupAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: TieredInspectionGroup, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the tiered inspection group steps in order and keeps a trace of each. */
data class TieredInspectionGroupEngine(
    var steps: MutableList<TieredInspectionGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredInspectionGroup): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredInspectionGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredInspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredInspectionGroupEngine(flags: Registry): TieredInspectionGroupEngine {
    return TieredInspectionGroupEngine(
        steps = mutableListOf<TieredInspectionGroupAdjuster>(
            FlaggedTieredInspectionGroupAdjuster(flags, "tiered-inspection-group-pending", -6.8),
            MinimumTieredInspectionGroupAdjuster(37.3),
            TieredInspectionGroupDurationMinutesOffsetAdjuster(0.81),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 58708.4,
    )
}
