package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tire slot calculation. */
interface TireSlotAdjuster {
    fun describe(): String
    fun adjust(item: TireSlot, value: Double): Double
}

data class CapTireSlotAdjuster(var cap: Double) : TireSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireSlot, value: Double): Double = minOf(value, cap)
}

data class FlaggedTireSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryTireSlotAdjuster(var factors: MutableMap<String, Double>) : TireSlotAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: TireSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTireSlotAdjuster(var step: Double) : TireSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumTireSlotAdjuster(var minimum: Double) : TireSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tire slot steps in order and keeps a trace of each. */
data class TireSlotEngine(
    var steps: MutableList<TireSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireSlot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TireSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireSlotEngine(flags: Registry): TireSlotEngine {
    return TireSlotEngine(
        steps = mutableListOf<TireSlotAdjuster>(
            CapTireSlotAdjuster(38850.0),
            FlaggedTireSlotAdjuster(flags, "tire-slot-batched", 2.4),
            CountryTireSlotAdjuster(
                mutableMapOf<String, Double>("west" to 0.9, "east" to 1.24, "central" to 0.76),
            ),
            RoundingTireSlotAdjuster(1.0),
            MinimumTireSlotAdjuster(16.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 29835.1,
    )
}
