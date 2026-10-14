package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict ev charge slot calculation. */
interface StrictEvChargeSlotStep {
    fun describe(): String
    fun adjust(item: StrictEvChargeSlot, value: Double): Double
}

data class LabelStrictEvChargeSlotAdjuster(var factors: MutableMap<String, Double>) : StrictEvChargeSlotStep {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictEvChargeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictEvChargeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictEvChargeSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictEvChargeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictEvChargeSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictEvChargeSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictEvChargeSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictEvChargeSlotGraceDaysOffsetAdjuster(var weight: Double) : StrictEvChargeSlotStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: StrictEvChargeSlot, value: Double): Double = value + item.graceDays.toDouble() * weight
}

/** Runs the strict ev charge slot steps in order and keeps a trace of each. */
data class StrictEvChargeSlotEngine(
    var steps: MutableList<StrictEvChargeSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictEvChargeSlot): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictEvChargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictEvChargeSlotEngine(flags: Registry): StrictEvChargeSlotEngine {
    return StrictEvChargeSlotEngine(
        steps = mutableListOf<StrictEvChargeSlotStep>(
            LabelStrictEvChargeSlotAdjuster(
                mutableMapOf<String, Double>("south" to 0.76, "west" to 0.97, "retail" to 1.18),
            ),
            FlaggedStrictEvChargeSlotAdjuster(flags, "strict-ev-charge-slot-scheduled", 7.9),
            TieredStrictEvChargeSlotAdjuster(
                mutableListOf<Double>(463.0, 473.0),
                mutableListOf<Double>(1.04, 0.85),
            ),
            StrictEvChargeSlotGraceDaysOffsetAdjuster(0.49),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 96780.1,
    )
}
