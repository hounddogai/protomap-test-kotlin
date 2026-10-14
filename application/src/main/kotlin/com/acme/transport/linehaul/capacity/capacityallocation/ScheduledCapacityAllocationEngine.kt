package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled capacity allocation calculation. */
interface ScheduledCapacityAllocationModifier {
    fun describe(): String
    fun adjust(item: ScheduledCapacityAllocation, value: Double): Double
}

data class FlaggedScheduledCapacityAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledCapacityAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledCapacityAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledCapacityAllocationDurationMinutesOffsetAdjuster(
    var weight: Double,
) : ScheduledCapacityAllocationModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ScheduledCapacityAllocation, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class RoundingScheduledCapacityAllocationAdjuster(var step: Double) : ScheduledCapacityAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledCapacityAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledCapacityAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCapacityAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCapacityAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled capacity allocation steps in order and keeps a trace of each. */
data class ScheduledCapacityAllocationEngine(
    var steps: MutableList<ScheduledCapacityAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledCapacityAllocation): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledCapacityAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledCapacityAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledCapacityAllocationEngine(flags: Registry): ScheduledCapacityAllocationEngine {
    return ScheduledCapacityAllocationEngine(
        steps = mutableListOf<ScheduledCapacityAllocationModifier>(
            FlaggedScheduledCapacityAllocationAdjuster(flags, "scheduled-capacity-allocation-weighted", 4.9),
            ScheduledCapacityAllocationDurationMinutesOffsetAdjuster(0.02),
            RoundingScheduledCapacityAllocationAdjuster(1.0),
            TieredScheduledCapacityAllocationAdjuster(
                mutableListOf<Double>(79.0, 1401.0, 2623.0, 8984.0),
                mutableListOf<Double>(0.96, 0.88, 0.82, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 26085.7,
    )
}
