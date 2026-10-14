package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.flags.Registry

/** One step of the rate card reservation calculation. */
interface RateCardReservationFactor {
    fun describe(): String
    fun adjust(item: RateCardReservation, value: Double): Double
}

data class CapRateCardReservationAdjuster(var cap: Double) : RateCardReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardReservation, value: Double): Double = minOf(value, cap)
}

data class RateCardReservationExpiresAtOffsetAdjuster(var weight: Double) : RateCardReservationFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RateCardReservation, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class FlaggedRateCardReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rate card reservation steps in order and keeps a trace of each. */
data class RateCardReservationEngine(
    var steps: MutableList<RateCardReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardReservation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardReservationEngine(flags: Registry): RateCardReservationEngine {
    return RateCardReservationEngine(
        steps = mutableListOf<RateCardReservationFactor>(
            CapRateCardReservationAdjuster(48091.0),
            RateCardReservationExpiresAtOffsetAdjuster(1.86),
            FlaggedRateCardReservationAdjuster(flags, "rate-card-reservation-rolling", 3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 11720.7,
    )
}
