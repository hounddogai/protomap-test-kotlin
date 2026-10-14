package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional hold slot calculation. */
interface RegionalHoldSlotModifier {
    fun describe(): String
    fun adjust(item: RegionalHoldSlot, value: Double): Double
}

data class MinimumRegionalHoldSlotAdjuster(var minimum: Double) : RegionalHoldSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalHoldSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalHoldSlotAdjuster(var step: Double) : RegionalHoldSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalHoldSlot, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalHoldSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalHoldSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalHoldSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRegionalHoldSlotAdjuster(var factors: MutableMap<String, Double>) : RegionalHoldSlotModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalHoldSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional hold slot steps in order and keeps a trace of each. */
data class RegionalHoldSlotEngine(
    var steps: MutableList<RegionalHoldSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalHoldSlot): Double {
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

    fun evaluateAll(items: MutableList<RegionalHoldSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalHoldSlotEngine(flags: Registry): RegionalHoldSlotEngine {
    return RegionalHoldSlotEngine(
        steps = mutableListOf<RegionalHoldSlotModifier>(
            MinimumRegionalHoldSlotAdjuster(39.4),
            RoundingRegionalHoldSlotAdjuster(0.25),
            FlaggedRegionalHoldSlotAdjuster(flags, "regional-hold-slot-default", -8.5),
            NameRegionalHoldSlotAdjuster(
                mutableMapOf<String, Double>("east" to 1.0, "west" to 1.15, "retail" to 0.71),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 83564.4,
    )
}
