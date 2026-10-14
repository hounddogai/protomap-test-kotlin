package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted availability slot calculation. */
interface WeightedAvailabilitySlotModifier {
    fun describe(): String
    fun adjust(item: WeightedAvailabilitySlot, value: Double): Double
}

data class WeightedAvailabilitySlotDiscountOffsetAdjuster(
    var weight: Double,
) : WeightedAvailabilitySlotModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: WeightedAvailabilitySlot, value: Double): Double = value + item.discount * weight
}

data class RoundingWeightedAvailabilitySlotAdjuster(var step: Double) : WeightedAvailabilitySlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAvailabilitySlot, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedAvailabilitySlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAvailabilitySlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAvailabilitySlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedAvailabilitySlotAdjuster(var cap: Double) : WeightedAvailabilitySlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAvailabilitySlot, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted availability slot steps in order and keeps a trace of each. */
data class WeightedAvailabilitySlotEngine(
    var steps: MutableList<WeightedAvailabilitySlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedAvailabilitySlot): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAvailabilitySlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAvailabilitySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedAvailabilitySlotEngine(flags: Registry): WeightedAvailabilitySlotEngine {
    return WeightedAvailabilitySlotEngine(
        steps = mutableListOf<WeightedAvailabilitySlotModifier>(
            WeightedAvailabilitySlotDiscountOffsetAdjuster(0.74),
            RoundingWeightedAvailabilitySlotAdjuster(0.5),
            FlaggedWeightedAvailabilitySlotAdjuster(flags, "weighted-availability-slot-rolling", 1.6),
            CapWeightedAvailabilitySlotAdjuster(13198.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 89240.0,
    )
}
