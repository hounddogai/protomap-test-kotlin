package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the locker slot calculation. */
interface LockerSlotAdjuster {
    fun describe(): String
    fun adjust(item: LockerSlot, value: Double): Double
}

data class LocaleLockerSlotAdjuster(var factors: MutableMap<String, Double>) : LockerSlotAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: LockerSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LockerSlotLoadKgOffsetAdjuster(var weight: Double) : LockerSlotAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LockerSlot, value: Double): Double = value + item.loadKg * weight
}

data class CapLockerSlotAdjuster(var cap: Double) : LockerSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerSlot, value: Double): Double = minOf(value, cap)
}

data class MinimumLockerSlotAdjuster(var minimum: Double) : LockerSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLockerSlotAdjuster(var percent: Double) : LockerSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLockerSlotAdjuster(var step: Double) : LockerSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the locker slot steps in order and keeps a trace of each. */
data class LockerSlotEngine(
    var steps: MutableList<LockerSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerSlot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerSlotEngine(): LockerSlotEngine {
    return LockerSlotEngine(
        steps = mutableListOf<LockerSlotAdjuster>(
            LocaleLockerSlotAdjuster(mutableMapOf<String, Double>("south" to 1.15, "retail" to 0.7)),
            LockerSlotLoadKgOffsetAdjuster(0.28),
            CapLockerSlotAdjuster(17197.0),
            MinimumLockerSlotAdjuster(19.0),
            PercentLockerSlotAdjuster(-23.3),
            RoundingLockerSlotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 36873.5,
    )
}
