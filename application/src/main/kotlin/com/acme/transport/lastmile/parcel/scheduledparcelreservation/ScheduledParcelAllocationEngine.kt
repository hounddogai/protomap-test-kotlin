package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled parcel allocation calculation. */
interface ScheduledParcelAllocationAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledParcelAllocation, value: Double): Double
}

data class RoundingScheduledParcelAllocationAdjuster(var step: Double) : ScheduledParcelAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledParcelAllocation, value: Double): Double = roundTo(value, step)
}

data class PercentScheduledParcelAllocationAdjuster(var percent: Double) : ScheduledParcelAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledParcelAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledParcelAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledParcelAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledParcelAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledParcelAllocationAdjuster(var cap: Double) : ScheduledParcelAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledParcelAllocation, value: Double): Double = minOf(value, cap)
}

data class LocaleScheduledParcelAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledParcelAllocationAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: ScheduledParcelAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledParcelAllocationFuelLitersOffsetAdjuster(
    var weight: Double,
) : ScheduledParcelAllocationAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: ScheduledParcelAllocation, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the scheduled parcel allocation steps in order and keeps a trace of each. */
data class ScheduledParcelAllocationEngine(
    var steps: MutableList<ScheduledParcelAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledParcelAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledParcelAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledParcelAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledParcelAllocationEngine(flags: Registry): ScheduledParcelAllocationEngine {
    return ScheduledParcelAllocationEngine(
        steps = mutableListOf<ScheduledParcelAllocationAdjuster>(
            RoundingScheduledParcelAllocationAdjuster(0.05),
            PercentScheduledParcelAllocationAdjuster(24.2),
            FlaggedScheduledParcelAllocationAdjuster(flags, "scheduled-parcel-allocation-capped", 1.9),
            CapScheduledParcelAllocationAdjuster(29240.0),
            LocaleScheduledParcelAllocationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.13, "north" to 1.04),
            ),
            ScheduledParcelAllocationFuelLitersOffsetAdjuster(1.13),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 53112.7,
    )
}
