package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge slot calculation. */
interface EvChargeSlotStep {
    fun describe(): String
    fun adjust(item: EvChargeSlot, value: Double): Double
}

data class FlaggedEvChargeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumEvChargeSlotAdjuster(var minimum: Double) : EvChargeSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class EvChargeSlotRevisionOffsetAdjuster(var weight: Double) : EvChargeSlotStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: EvChargeSlot, value: Double): Double = value + item.revision.toDouble() * weight
}

data class CapEvChargeSlotAdjuster(var cap: Double) : EvChargeSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge slot steps in order and keeps a trace of each. */
data class EvChargeSlotEngine(
    var steps: MutableList<EvChargeSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargeSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargeSlotEngine(flags: Registry): EvChargeSlotEngine {
    return EvChargeSlotEngine(
        steps = mutableListOf<EvChargeSlotStep>(
            FlaggedEvChargeSlotAdjuster(flags, "ev-charge-slot-rolling", -5.0),
            MinimumEvChargeSlotAdjuster(32.5),
            EvChargeSlotRevisionOffsetAdjuster(1.27),
            CapEvChargeSlotAdjuster(42249.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 29103.1,
    )
}
