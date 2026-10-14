package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled zone reservation calculation. */
interface ScheduledZoneReservationFactor {
    fun describe(): String
    fun adjust(item: ScheduledZoneReservation, value: Double): Double
}

data class FlaggedScheduledZoneReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledZoneReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledZoneReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledZoneReservationAdjuster(var percent: Double) : ScheduledZoneReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledZoneReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledZoneReservationRetriesOffsetAdjuster(var weight: Double) : ScheduledZoneReservationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ScheduledZoneReservation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class MinimumScheduledZoneReservationAdjuster(var minimum: Double) : ScheduledZoneReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledZoneReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled zone reservation steps in order and keeps a trace of each. */
data class ScheduledZoneReservationEngine(
    var steps: MutableList<ScheduledZoneReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledZoneReservation): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledZoneReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledZoneReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledZoneReservationEngine(flags: Registry): ScheduledZoneReservationEngine {
    return ScheduledZoneReservationEngine(
        steps = mutableListOf<ScheduledZoneReservationFactor>(
            FlaggedScheduledZoneReservationAdjuster(flags, "scheduled-zone-reservation-lenient", -8.4),
            PercentScheduledZoneReservationAdjuster(-9.9),
            ScheduledZoneReservationRetriesOffsetAdjuster(0.07),
            MinimumScheduledZoneReservationAdjuster(24.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 42584.0,
    )
}
