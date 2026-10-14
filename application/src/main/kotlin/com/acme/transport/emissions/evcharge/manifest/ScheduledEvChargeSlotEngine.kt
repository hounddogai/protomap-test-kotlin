package com.acme.transport.emissions.evcharge.manifest

/** One step of the scheduled ev charge slot calculation. */
interface ScheduledEvChargeSlotStep {
    fun describe(): String
    fun adjust(item: ScheduledEvChargeSlot, value: Double): Double
}

data class MinimumScheduledEvChargeSlotAdjuster(var minimum: Double) : ScheduledEvChargeSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledEvChargeSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledEvChargeSlotAdjuster(var percent: Double) : ScheduledEvChargeSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledEvChargeSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledEvChargeSlotAdjuster(var cap: Double) : ScheduledEvChargeSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledEvChargeSlot, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledEvChargeSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledEvChargeSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledEvChargeSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled ev charge slot steps in order and keeps a trace of each. */
data class ScheduledEvChargeSlotEngine(
    var steps: MutableList<ScheduledEvChargeSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledEvChargeSlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledEvChargeSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledEvChargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledEvChargeSlotEngine(): ScheduledEvChargeSlotEngine {
    return ScheduledEvChargeSlotEngine(
        steps = mutableListOf<ScheduledEvChargeSlotStep>(
            MinimumScheduledEvChargeSlotAdjuster(27.0),
            PercentScheduledEvChargeSlotAdjuster(4.0),
            CapScheduledEvChargeSlotAdjuster(5230.0),
            TieredScheduledEvChargeSlotAdjuster(
                mutableListOf<Double>(3524.0, 4506.0, 5689.0),
                mutableListOf<Double>(0.86, 1.03, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 6469.8,
    )
}
