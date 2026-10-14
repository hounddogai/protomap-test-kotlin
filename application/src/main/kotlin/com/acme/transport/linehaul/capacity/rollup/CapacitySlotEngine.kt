package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity slot calculation. */
interface CapacitySlotFactor {
    fun describe(): String
    fun adjust(item: CapacitySlot, value: Double): Double
}

data class FlaggedCapacitySlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacitySlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacitySlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapacitySlotEndsAtOffsetAdjuster(var weight: Double) : CapacitySlotFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CapacitySlot, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class MinimumCapacitySlotAdjuster(var minimum: Double) : CapacitySlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacitySlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelCapacitySlotAdjuster(var factors: MutableMap<String, Double>) : CapacitySlotFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CapacitySlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capacity slot steps in order and keeps a trace of each. */
data class CapacitySlotEngine(
    var steps: MutableList<CapacitySlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacitySlot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacitySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacitySlotEngine(flags: Registry): CapacitySlotEngine {
    return CapacitySlotEngine(
        steps = mutableListOf<CapacitySlotFactor>(
            FlaggedCapacitySlotAdjuster(flags, "capacity-slot-seasonal", -1.4),
            CapacitySlotEndsAtOffsetAdjuster(1.32),
            MinimumCapacitySlotAdjuster(5.0),
            LabelCapacitySlotAdjuster(mutableMapOf<String, Double>("north" to 1.3)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 83086.1,
    )
}
