package com.acme.transport.crewing.availability.mapping

/** One step of the scheduled availability profile calculation. */
interface ScheduledAvailabilityProfileAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledAvailabilityProfile, value: Double): Double
}

data class RegionScheduledAvailabilityProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledAvailabilityProfileAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledAvailabilityProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledAvailabilityProfileAdjuster(var cap: Double) : ScheduledAvailabilityProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledAvailabilityProfile, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledAvailabilityProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledAvailabilityProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledAvailabilityProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled availability profile steps in order and keeps a trace of each. */
data class ScheduledAvailabilityProfileEngine(
    var steps: MutableList<ScheduledAvailabilityProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledAvailabilityProfile): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledAvailabilityProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledAvailabilityProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledAvailabilityProfileEngine(): ScheduledAvailabilityProfileEngine {
    return ScheduledAvailabilityProfileEngine(
        steps = mutableListOf<ScheduledAvailabilityProfileAdjuster>(
            RegionScheduledAvailabilityProfileAdjuster(
                mutableMapOf<String, Double>("east" to 0.7, "south" to 1.21, "online" to 1.2),
            ),
            CapScheduledAvailabilityProfileAdjuster(41963.0),
            TieredScheduledAvailabilityProfileAdjuster(
                mutableListOf<Double>(3473.0, 3579.0, 4535.0, 6466.0),
                mutableListOf<Double>(0.99, 1.06, 0.83, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 80293.9,
    )
}
