package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling slot calculation. */
interface IdlingSlotFactor {
    fun describe(): String
    fun adjust(item: IdlingSlot, value: Double): Double
}

data class RoundingIdlingSlotAdjuster(var step: Double) : IdlingSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingSlot, value: Double): Double = roundTo(value, step)
}

data class CapIdlingSlotAdjuster(var cap: Double) : IdlingSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingSlot, value: Double): Double = minOf(value, cap)
}

data class FlaggedIdlingSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIdlingSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the idling slot steps in order and keeps a trace of each. */
data class IdlingSlotEngine(
    var steps: MutableList<IdlingSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingSlot): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingSlotEngine(flags: Registry): IdlingSlotEngine {
    return IdlingSlotEngine(
        steps = mutableListOf<IdlingSlotFactor>(
            RoundingIdlingSlotAdjuster(0.05),
            CapIdlingSlotAdjuster(11652.0),
            FlaggedIdlingSlotAdjuster(flags, "idling-slot-scheduled", 6.2),
            TieredIdlingSlotAdjuster(
                mutableListOf<Double>(264.0, 4006.0, 6472.0),
                mutableListOf<Double>(1.04, 0.86, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 48962.5,
    )
}
