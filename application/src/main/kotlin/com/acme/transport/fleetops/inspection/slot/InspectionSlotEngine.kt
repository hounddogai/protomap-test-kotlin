package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection slot calculation. */
interface InspectionSlotAdjuster {
    fun describe(): String
    fun adjust(item: InspectionSlot, value: Double): Double
}

data class FlaggedInspectionSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumInspectionSlotAdjuster(var minimum: Double) : InspectionSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameInspectionSlotAdjuster(var factors: MutableMap<String, Double>) : InspectionSlotAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: InspectionSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the inspection slot steps in order and keeps a trace of each. */
data class InspectionSlotEngine(
    var steps: MutableList<InspectionSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionSlotEngine(flags: Registry): InspectionSlotEngine {
    return InspectionSlotEngine(
        steps = mutableListOf<InspectionSlotAdjuster>(
            FlaggedInspectionSlotAdjuster(flags, "inspection-slot-fallback", -6.7),
            MinimumInspectionSlotAdjuster(4.9),
            NameInspectionSlotAdjuster(mutableMapOf<String, Double>("north" to 1.12)),
            TieredInspectionSlotAdjuster(
                mutableListOf<Double>(6919.0, 9296.0, 9384.0),
                mutableListOf<Double>(0.85, 1.18, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 86504.8,
    )
}
