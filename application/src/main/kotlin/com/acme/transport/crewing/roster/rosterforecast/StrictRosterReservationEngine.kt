package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.flags.Registry

/** One step of the strict roster reservation calculation. */
interface StrictRosterReservationAdjuster {
    fun describe(): String
    fun adjust(item: StrictRosterReservation, value: Double): Double
}

data class TieredStrictRosterReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRosterReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRosterReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictRosterReservationAdjuster(var minimum: Double) : StrictRosterReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRosterReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictRosterReservationExpiresAtOffsetAdjuster(
    var weight: Double,
) : StrictRosterReservationAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: StrictRosterReservation, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class PercentStrictRosterReservationAdjuster(var percent: Double) : StrictRosterReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRosterReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictRosterReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRosterReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRosterReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapStrictRosterReservationAdjuster(var cap: Double) : StrictRosterReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRosterReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the strict roster reservation steps in order and keeps a trace of each. */
data class StrictRosterReservationEngine(
    var steps: MutableList<StrictRosterReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictRosterReservation): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRosterReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRosterReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictRosterReservationEngine(flags: Registry): StrictRosterReservationEngine {
    return StrictRosterReservationEngine(
        steps = mutableListOf<StrictRosterReservationAdjuster>(
            TieredStrictRosterReservationAdjuster(
                mutableListOf<Double>(1207.0, 3596.0),
                mutableListOf<Double>(1.08, 0.89),
            ),
            MinimumStrictRosterReservationAdjuster(4.5),
            StrictRosterReservationExpiresAtOffsetAdjuster(1.99),
            PercentStrictRosterReservationAdjuster(24.9),
            FlaggedStrictRosterReservationAdjuster(flags, "strict-roster-reservation-lenient", -7.6),
            CapStrictRosterReservationAdjuster(40921.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 82155.1,
    )
}
