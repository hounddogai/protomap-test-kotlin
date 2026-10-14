package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the availability slot calculation. */
interface AvailabilitySlotModifier {
    fun describe(): String
    fun adjust(item: AvailabilitySlot, value: Double): Double
}

data class LabelAvailabilitySlotAdjuster(var factors: MutableMap<String, Double>) : AvailabilitySlotModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: AvailabilitySlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAvailabilitySlotAdjuster(var percent: Double) : AvailabilitySlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilitySlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AvailabilitySlotGraceDaysOffsetAdjuster(var weight: Double) : AvailabilitySlotModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: AvailabilitySlot, value: Double): Double = value + item.graceDays.toDouble() * weight
}

/** Runs the availability slot steps in order and keeps a trace of each. */
data class AvailabilitySlotEngine(
    var steps: MutableList<AvailabilitySlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilitySlot): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilitySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilitySlotEngine(): AvailabilitySlotEngine {
    return AvailabilitySlotEngine(
        steps = mutableListOf<AvailabilitySlotModifier>(
            LabelAvailabilitySlotAdjuster(mutableMapOf<String, Double>("north" to 1.01, "online" to 0.97)),
            PercentAvailabilitySlotAdjuster(-22.9),
            AvailabilitySlotGraceDaysOffsetAdjuster(1.63),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 91654.9,
    )
}
