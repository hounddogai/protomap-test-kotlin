package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.flags.Registry

/** One step of the rolling tire reservation calculation. */
interface RollingTireReservationStep {
    fun describe(): String
    fun adjust(item: RollingTireReservation, value: Double): Double
}

data class MinimumRollingTireReservationAdjuster(var minimum: Double) : RollingTireReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTireReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingTireReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTireReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTireReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingTireReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTireReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTireReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingTireReservationPalletsOffsetAdjuster(var weight: Double) : RollingTireReservationStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: RollingTireReservation, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentRollingTireReservationAdjuster(var percent: Double) : RollingTireReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTireReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling tire reservation steps in order and keeps a trace of each. */
data class RollingTireReservationEngine(
    var steps: MutableList<RollingTireReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingTireReservation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTireReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTireReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingTireReservationEngine(flags: Registry): RollingTireReservationEngine {
    return RollingTireReservationEngine(
        steps = mutableListOf<RollingTireReservationStep>(
            MinimumRollingTireReservationAdjuster(39.6),
            FlaggedRollingTireReservationAdjuster(flags, "rolling-tire-reservation-regional", -7.7),
            TieredRollingTireReservationAdjuster(
                mutableListOf<Double>(4220.0, 5494.0, 7120.0, 8923.0),
                mutableListOf<Double>(0.83, 1.01, 1.06, 0.84),
            ),
            RollingTireReservationPalletsOffsetAdjuster(0.3),
            PercentRollingTireReservationAdjuster(12.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 28013.0,
    )
}
