package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.flags.Registry

/** One step of the capacity profile calculation. */
interface CapacityProfileAdjuster {
    fun describe(): String
    fun adjust(item: CapacityProfile, value: Double): Double
}

data class MinimumCapacityProfileAdjuster(var minimum: Double) : CapacityProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCapacityProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapacityProfileCeilingOffsetAdjuster(var weight: Double) : CapacityProfileAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CapacityProfile, value: Double): Double = value + item.ceiling * weight
}

/** Runs the capacity profile steps in order and keeps a trace of each. */
data class CapacityProfileEngine(
    var steps: MutableList<CapacityProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityProfile): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityProfileEngine(flags: Registry): CapacityProfileEngine {
    return CapacityProfileEngine(
        steps = mutableListOf<CapacityProfileAdjuster>(
            MinimumCapacityProfileAdjuster(26.3),
            FlaggedCapacityProfileAdjuster(flags, "capacity-profile-scheduled", -2.1),
            CapacityProfileCeilingOffsetAdjuster(1.58),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 55819.9,
    )
}
