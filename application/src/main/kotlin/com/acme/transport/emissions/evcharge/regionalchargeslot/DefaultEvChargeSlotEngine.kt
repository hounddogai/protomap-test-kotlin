package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default ev charge slot calculation. */
interface DefaultEvChargeSlotModifier {
    fun describe(): String
    fun adjust(item: DefaultEvChargeSlot, value: Double): Double
}

data class DefaultEvChargeSlotDistanceKmOffsetAdjuster(var weight: Double) : DefaultEvChargeSlotModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DefaultEvChargeSlot, value: Double): Double = value + item.distanceKm * weight
}

data class MinimumDefaultEvChargeSlotAdjuster(var minimum: Double) : DefaultEvChargeSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultEvChargeSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultEvChargeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultEvChargeSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultEvChargeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultEvChargeSlotAdjuster(var percent: Double) : DefaultEvChargeSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultEvChargeSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultEvChargeSlotAdjuster(var cap: Double) : DefaultEvChargeSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultEvChargeSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultEvChargeSlotAdjuster(var step: Double) : DefaultEvChargeSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultEvChargeSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the default ev charge slot steps in order and keeps a trace of each. */
data class DefaultEvChargeSlotEngine(
    var steps: MutableList<DefaultEvChargeSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultEvChargeSlot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultEvChargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultEvChargeSlotEngine(flags: Registry): DefaultEvChargeSlotEngine {
    return DefaultEvChargeSlotEngine(
        steps = mutableListOf<DefaultEvChargeSlotModifier>(
            DefaultEvChargeSlotDistanceKmOffsetAdjuster(1.03),
            MinimumDefaultEvChargeSlotAdjuster(47.6),
            FlaggedDefaultEvChargeSlotAdjuster(flags, "default-ev-charge-slot-strict", -9.0),
            PercentDefaultEvChargeSlotAdjuster(3.1),
            CapDefaultEvChargeSlotAdjuster(22980.0),
            RoundingDefaultEvChargeSlotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 67919.1,
    )
}
