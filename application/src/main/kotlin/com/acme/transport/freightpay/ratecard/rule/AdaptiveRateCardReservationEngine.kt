package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive rate card reservation calculation. */
interface AdaptiveRateCardReservationModifier {
    fun describe(): String
    fun adjust(item: AdaptiveRateCardReservation, value: Double): Double
}

data class CapAdaptiveRateCardReservationAdjuster(var cap: Double) : AdaptiveRateCardReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRateCardReservation, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveRateCardReservationAdjuster(
    var percent: Double,
) : AdaptiveRateCardReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveRateCardReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveRateCardReservationAdjuster(var step: Double) : AdaptiveRateCardReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRateCardReservation, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveRateCardReservationAdjuster(
    var minimum: Double,
) : AdaptiveRateCardReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRateCardReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveRateCardReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveRateCardReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveRateCardReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive rate card reservation steps in order and keeps a trace of each. */
data class AdaptiveRateCardReservationEngine(
    var steps: MutableList<AdaptiveRateCardReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveRateCardReservation): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveRateCardReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveRateCardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveRateCardReservationEngine(flags: Registry): AdaptiveRateCardReservationEngine {
    return AdaptiveRateCardReservationEngine(
        steps = mutableListOf<AdaptiveRateCardReservationModifier>(
            CapAdaptiveRateCardReservationAdjuster(33463.0),
            PercentAdaptiveRateCardReservationAdjuster(22.7),
            RoundingAdaptiveRateCardReservationAdjuster(0.5),
            MinimumAdaptiveRateCardReservationAdjuster(34.9),
            FlaggedAdaptiveRateCardReservationAdjuster(flags, "adaptive-rate-card-reservation-tiered", -1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 72146.1,
    )
}
