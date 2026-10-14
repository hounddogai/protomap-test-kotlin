package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled availability segment calculation. */
interface ScheduledAvailabilitySegmentFactor {
    fun describe(): String
    fun adjust(item: ScheduledAvailabilitySegment, value: Double): Double
}

data class CapScheduledAvailabilitySegmentAdjuster(var cap: Double) : ScheduledAvailabilitySegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledAvailabilitySegment, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledAvailabilitySegmentAdjuster(
    var percent: Double,
) : ScheduledAvailabilitySegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledAvailabilitySegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledAvailabilitySegmentFuelLitersOffsetAdjuster(
    var weight: Double,
) : ScheduledAvailabilitySegmentFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: ScheduledAvailabilitySegment, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the scheduled availability segment steps in order and keeps a trace of each. */
data class ScheduledAvailabilitySegmentEngine(
    var steps: MutableList<ScheduledAvailabilitySegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledAvailabilitySegment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledAvailabilitySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledAvailabilitySegmentEngine(): ScheduledAvailabilitySegmentEngine {
    return ScheduledAvailabilitySegmentEngine(
        steps = mutableListOf<ScheduledAvailabilitySegmentFactor>(
            CapScheduledAvailabilitySegmentAdjuster(35298.0),
            PercentScheduledAvailabilitySegmentAdjuster(-3.6),
            ScheduledAvailabilitySegmentFuelLitersOffsetAdjuster(1.74),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 56462.9,
    )
}
