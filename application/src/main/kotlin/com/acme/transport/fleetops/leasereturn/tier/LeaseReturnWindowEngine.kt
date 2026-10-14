package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return window calculation. */
interface LeaseReturnWindowModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnWindow, value: Double): Double
}

data class LeaseReturnWindowDurationMinutesOffsetAdjuster(var weight: Double) : LeaseReturnWindowModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: LeaseReturnWindow, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedLeaseReturnWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLeaseReturnWindowAdjuster(var step: Double) : LeaseReturnWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnWindow, value: Double): Double = roundTo(value, step)
}

data class CapLeaseReturnWindowAdjuster(var cap: Double) : LeaseReturnWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumLeaseReturnWindowAdjuster(var minimum: Double) : LeaseReturnWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lease return window steps in order and keeps a trace of each. */
data class LeaseReturnWindowEngine(
    var steps: MutableList<LeaseReturnWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnWindow): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnWindowEngine(flags: Registry): LeaseReturnWindowEngine {
    return LeaseReturnWindowEngine(
        steps = mutableListOf<LeaseReturnWindowModifier>(
            LeaseReturnWindowDurationMinutesOffsetAdjuster(1.2),
            FlaggedLeaseReturnWindowAdjuster(flags, "lease-return-window-pending", -6.6),
            RoundingLeaseReturnWindowAdjuster(1.0),
            CapLeaseReturnWindowAdjuster(48870.0),
            MinimumLeaseReturnWindowAdjuster(24.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 62522.3,
    )
}
