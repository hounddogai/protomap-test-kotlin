package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient dock door slot calculation. */
interface LenientDockDoorSlotFactor {
    fun describe(): String
    fun adjust(item: LenientDockDoorSlot, value: Double): Double
}

data class FlaggedLenientDockDoorSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDockDoorSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDockDoorSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentLenientDockDoorSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDockDoorSlotFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: LenientDockDoorSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientDockDoorSlotAdjuster(var minimum: Double) : LenientDockDoorSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDockDoorSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientDockDoorSlotAdjuster(var step: Double) : LenientDockDoorSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDockDoorSlot, value: Double): Double = roundTo(value, step)
}

data class CapLenientDockDoorSlotAdjuster(var cap: Double) : LenientDockDoorSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDockDoorSlot, value: Double): Double = minOf(value, cap)
}

data class LenientDockDoorSlotBatchSizeOffsetAdjuster(var weight: Double) : LenientDockDoorSlotFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LenientDockDoorSlot, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the lenient dock door slot steps in order and keeps a trace of each. */
data class LenientDockDoorSlotEngine(
    var steps: MutableList<LenientDockDoorSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDockDoorSlot): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDockDoorSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDockDoorSlotEngine(flags: Registry): LenientDockDoorSlotEngine {
    return LenientDockDoorSlotEngine(
        steps = mutableListOf<LenientDockDoorSlotFactor>(
            FlaggedLenientDockDoorSlotAdjuster(flags, "lenient-dock-door-slot-rolling", -5.5),
            SegmentLenientDockDoorSlotAdjuster(
                mutableMapOf<String, Double>("online" to 1.34, "south" to 0.88, "north" to 0.72),
            ),
            MinimumLenientDockDoorSlotAdjuster(12.1),
            RoundingLenientDockDoorSlotAdjuster(0.5),
            CapLenientDockDoorSlotAdjuster(49021.0),
            LenientDockDoorSlotBatchSizeOffsetAdjuster(0.56),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 75579.9,
    )
}
