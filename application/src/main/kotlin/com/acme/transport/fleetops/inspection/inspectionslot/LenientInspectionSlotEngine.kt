package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.flags.Registry

/** One step of the lenient inspection slot calculation. */
interface LenientInspectionSlotFactor {
    fun describe(): String
    fun adjust(item: LenientInspectionSlot, value: Double): Double
}

data class LenientInspectionSlotStartsAtOffsetAdjuster(var weight: Double) : LenientInspectionSlotFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: LenientInspectionSlot, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapLenientInspectionSlotAdjuster(var cap: Double) : LenientInspectionSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientInspectionSlot, value: Double): Double = minOf(value, cap)
}

data class TieredLenientInspectionSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientInspectionSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientInspectionSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientInspectionSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientInspectionSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientInspectionSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient inspection slot steps in order and keeps a trace of each. */
data class LenientInspectionSlotEngine(
    var steps: MutableList<LenientInspectionSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientInspectionSlot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientInspectionSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientInspectionSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientInspectionSlotEngine(flags: Registry): LenientInspectionSlotEngine {
    return LenientInspectionSlotEngine(
        steps = mutableListOf<LenientInspectionSlotFactor>(
            LenientInspectionSlotStartsAtOffsetAdjuster(1.56),
            CapLenientInspectionSlotAdjuster(42225.0),
            TieredLenientInspectionSlotAdjuster(
                mutableListOf<Double>(1507.0, 2043.0),
                mutableListOf<Double>(1.04, 1.18),
            ),
            FlaggedLenientInspectionSlotAdjuster(flags, "lenient-inspection-slot-global", 5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 51506.1,
    )
}
