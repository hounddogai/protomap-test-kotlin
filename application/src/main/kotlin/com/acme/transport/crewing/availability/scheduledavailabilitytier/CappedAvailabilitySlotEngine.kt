package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped availability slot calculation. */
interface CappedAvailabilitySlotStep {
    fun describe(): String
    fun adjust(item: CappedAvailabilitySlot, value: Double): Double
}

data class RoundingCappedAvailabilitySlotAdjuster(var step: Double) : CappedAvailabilitySlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAvailabilitySlot, value: Double): Double = roundTo(value, step)
}

data class CapCappedAvailabilitySlotAdjuster(var cap: Double) : CappedAvailabilitySlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAvailabilitySlot, value: Double): Double = minOf(value, cap)
}

data class ChannelCappedAvailabilitySlotAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAvailabilitySlotStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: CappedAvailabilitySlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedAvailabilitySlotAdjuster(var minimum: Double) : CappedAvailabilitySlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedAvailabilitySlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedAvailabilitySlotDurationMinutesOffsetAdjuster(
    var weight: Double,
) : CappedAvailabilitySlotStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: CappedAvailabilitySlot, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the capped availability slot steps in order and keeps a trace of each. */
data class CappedAvailabilitySlotEngine(
    var steps: MutableList<CappedAvailabilitySlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedAvailabilitySlot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAvailabilitySlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAvailabilitySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedAvailabilitySlotEngine(): CappedAvailabilitySlotEngine {
    return CappedAvailabilitySlotEngine(
        steps = mutableListOf<CappedAvailabilitySlotStep>(
            RoundingCappedAvailabilitySlotAdjuster(0.5),
            CapCappedAvailabilitySlotAdjuster(43355.0),
            ChannelCappedAvailabilitySlotAdjuster(
                mutableMapOf<String, Double>("online" to 1.17, "retail" to 1.04),
            ),
            MinimumCappedAvailabilitySlotAdjuster(0.4),
            CappedAvailabilitySlotDurationMinutesOffsetAdjuster(1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 15944.1,
    )
}
