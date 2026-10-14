package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending tire slot calculation. */
interface PendingTireSlotFactor {
    fun describe(): String
    fun adjust(item: PendingTireSlot, value: Double): Double
}

data class ChannelPendingTireSlotAdjuster(var factors: MutableMap<String, Double>) : PendingTireSlotFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: PendingTireSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingTireSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTireSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTireSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingTireSlotAdjuster(var step: Double) : PendingTireSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTireSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingTireSlotAdjuster(var minimum: Double) : PendingTireSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTireSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingTireSlotPriorityOffsetAdjuster(var weight: Double) : PendingTireSlotFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: PendingTireSlot, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TieredPendingTireSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTireSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTireSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending tire slot steps in order and keeps a trace of each. */
data class PendingTireSlotEngine(
    var steps: MutableList<PendingTireSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingTireSlot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTireSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingTireSlotEngine(flags: Registry): PendingTireSlotEngine {
    return PendingTireSlotEngine(
        steps = mutableListOf<PendingTireSlotFactor>(
            ChannelPendingTireSlotAdjuster(mutableMapOf<String, Double>("east" to 0.8, "retail" to 1.09)),
            FlaggedPendingTireSlotAdjuster(flags, "pending-tire-slot-seasonal", 7.2),
            RoundingPendingTireSlotAdjuster(0.05),
            MinimumPendingTireSlotAdjuster(31.9),
            PendingTireSlotPriorityOffsetAdjuster(0.67),
            TieredPendingTireSlotAdjuster(
                mutableListOf<Double>(1759.0, 2867.0, 2878.0),
                mutableListOf<Double>(1.06, 0.88, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 54161.3,
    )
}
