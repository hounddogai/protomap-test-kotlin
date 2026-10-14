package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop slot calculation. */
interface DropSlotFactor {
    fun describe(): String
    fun adjust(item: DropSlot, value: Double): Double
}

data class FlaggedDropSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDropSlotAdjuster(var minimum: Double) : DropSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDropSlotAdjuster(var step: Double) : DropSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropSlot, value: Double): Double = roundTo(value, step)
}

data class CapDropSlotAdjuster(var cap: Double) : DropSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropSlot, value: Double): Double = minOf(value, cap)
}

data class PercentDropSlotAdjuster(var percent: Double) : DropSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionDropSlotAdjuster(var factors: MutableMap<String, Double>) : DropSlotFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: DropSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the drop slot steps in order and keeps a trace of each. */
data class DropSlotEngine(
    var steps: MutableList<DropSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DropSlot): Double {
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

    fun evaluateAll(items: MutableList<DropSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDropSlotEngine(flags: Registry): DropSlotEngine {
    return DropSlotEngine(
        steps = mutableListOf<DropSlotFactor>(
            FlaggedDropSlotAdjuster(flags, "drop-slot-global", 4.5),
            MinimumDropSlotAdjuster(38.1),
            RoundingDropSlotAdjuster(5.0),
            CapDropSlotAdjuster(11024.0),
            PercentDropSlotAdjuster(18.1),
            RegionDropSlotAdjuster(
                mutableMapOf<String, Double>("north" to 1.15, "south" to 0.8, "retail" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 61118.4,
    )
}
