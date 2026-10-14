package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the manual zone allocation calculation. */
interface ManualZoneAllocationModifier {
    fun describe(): String
    fun adjust(item: ManualZoneAllocation, value: Double): Double
}

data class FlaggedManualZoneAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualZoneAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualZoneAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentManualZoneAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualZoneAllocationModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: ManualZoneAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualZoneAllocationLoadKgOffsetAdjuster(var weight: Double) : ManualZoneAllocationModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ManualZoneAllocation, value: Double): Double = value + item.loadKg * weight
}

/** Runs the manual zone allocation steps in order and keeps a trace of each. */
data class ManualZoneAllocationEngine(
    var steps: MutableList<ManualZoneAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualZoneAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualZoneAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualZoneAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualZoneAllocationEngine(flags: Registry): ManualZoneAllocationEngine {
    return ManualZoneAllocationEngine(
        steps = mutableListOf<ManualZoneAllocationModifier>(
            FlaggedManualZoneAllocationAdjuster(flags, "manual-zone-allocation-capped", -8.1),
            SegmentManualZoneAllocationAdjuster(
                mutableMapOf<String, Double>("east" to 1.28, "retail" to 1.07, "south" to 1.05),
            ),
            ManualZoneAllocationLoadKgOffsetAdjuster(0.23),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 72468.2,
    )
}
