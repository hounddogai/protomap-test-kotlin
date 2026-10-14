package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the backhaul reservation calculation. */
interface BackhaulReservationAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulReservation, value: Double): Double
}

data class FlaggedBackhaulReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBackhaulReservationAdjuster(var minimum: Double) : BackhaulReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBackhaulReservationAdjuster(var percent: Double) : BackhaulReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryBackhaulReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : BackhaulReservationAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: BackhaulReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the backhaul reservation steps in order and keeps a trace of each. */
data class BackhaulReservationEngine(
    var steps: MutableList<BackhaulReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulReservationEngine(flags: Registry): BackhaulReservationEngine {
    return BackhaulReservationEngine(
        steps = mutableListOf<BackhaulReservationAdjuster>(
            FlaggedBackhaulReservationAdjuster(flags, "backhaul-reservation-strict", -1.0),
            MinimumBackhaulReservationAdjuster(9.4),
            PercentBackhaulReservationAdjuster(-17.1),
            CountryBackhaulReservationAdjuster(mutableMapOf<String, Double>("central" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 57248.0,
    )
}
