package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.flags.Registry

/** One step of the capped availability reservation calculation. */
interface CappedAvailabilityReservationStep {
    fun describe(): String
    fun adjust(item: CappedAvailabilityReservation, value: Double): Double
}

data class FlaggedCappedAvailabilityReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAvailabilityReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAvailabilityReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedAvailabilityReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAvailabilityReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAvailabilityReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedAvailabilityReservationAdjuster(
    var percent: Double,
) : CappedAvailabilityReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedAvailabilityReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedAvailabilityReservationAdjuster(var cap: Double) : CappedAvailabilityReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAvailabilityReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the capped availability reservation steps in order and keeps a trace of each. */
data class CappedAvailabilityReservationEngine(
    var steps: MutableList<CappedAvailabilityReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedAvailabilityReservation): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAvailabilityReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAvailabilityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedAvailabilityReservationEngine(flags: Registry): CappedAvailabilityReservationEngine {
    return CappedAvailabilityReservationEngine(
        steps = mutableListOf<CappedAvailabilityReservationStep>(
            FlaggedCappedAvailabilityReservationAdjuster(flags, "capped-availability-reservation-global", -3.9),
            TieredCappedAvailabilityReservationAdjuster(
                mutableListOf<Double>(1175.0, 4789.0, 5218.0, 7886.0),
                mutableListOf<Double>(0.87, 1.19, 1.15, 1.09),
            ),
            PercentCappedAvailabilityReservationAdjuster(23.8),
            CapCappedAvailabilityReservationAdjuster(5609.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 13183.8,
    )
}
