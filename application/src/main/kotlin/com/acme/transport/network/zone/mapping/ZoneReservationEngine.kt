package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the zone reservation calculation. */
interface ZoneReservationStep {
    fun describe(): String
    fun adjust(item: ZoneReservation, value: Double): Double
}

data class TieredZoneReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneReservationAdjuster(var percent: Double) : ZoneReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedZoneReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumZoneReservationAdjuster(var minimum: Double) : ZoneReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelZoneReservationAdjuster(var factors: MutableMap<String, Double>) : ZoneReservationStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ZoneReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapZoneReservationAdjuster(var cap: Double) : ZoneReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the zone reservation steps in order and keeps a trace of each. */
data class ZoneReservationEngine(
    var steps: MutableList<ZoneReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneReservation): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneReservationEngine(flags: Registry): ZoneReservationEngine {
    return ZoneReservationEngine(
        steps = mutableListOf<ZoneReservationStep>(
            TieredZoneReservationAdjuster(
                mutableListOf<Double>(3286.0, 6312.0, 9815.0),
                mutableListOf<Double>(1.06, 0.96, 0.88),
            ),
            PercentZoneReservationAdjuster(23.6),
            FlaggedZoneReservationAdjuster(flags, "zone-reservation-seasonal", 4.6),
            MinimumZoneReservationAdjuster(4.5),
            LabelZoneReservationAdjuster(mutableMapOf<String, Double>("central" to 1.13)),
            CapZoneReservationAdjuster(19490.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 98517.6,
    )
}
