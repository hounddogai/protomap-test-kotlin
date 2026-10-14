package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive clearance reservation calculation. */
interface AdaptiveClearanceReservationFactor {
    fun describe(): String
    fun adjust(item: AdaptiveClearanceReservation, value: Double): Double
}

data class FlaggedAdaptiveClearanceReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveClearanceReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveClearanceReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveClearanceReservationAdjuster(
    var percent: Double,
) : AdaptiveClearanceReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveClearanceReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveClearanceReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveClearanceReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveClearanceReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveClearanceReservationAdjuster(
    var minimum: Double,
) : AdaptiveClearanceReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveClearanceReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveClearanceReservationAdjuster(var step: Double) : AdaptiveClearanceReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveClearanceReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive clearance reservation steps in order and keeps a trace of each. */
data class AdaptiveClearanceReservationEngine(
    var steps: MutableList<AdaptiveClearanceReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveClearanceReservation): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveClearanceReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveClearanceReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveClearanceReservationEngine(flags: Registry): AdaptiveClearanceReservationEngine {
    return AdaptiveClearanceReservationEngine(
        steps = mutableListOf<AdaptiveClearanceReservationFactor>(
            FlaggedAdaptiveClearanceReservationAdjuster(flags, "adaptive-clearance-reservation-primary", 0.1),
            PercentAdaptiveClearanceReservationAdjuster(-12.5),
            TieredAdaptiveClearanceReservationAdjuster(
                mutableListOf<Double>(1960.0, 4615.0, 8290.0, 9819.0),
                mutableListOf<Double>(1.07, 0.93, 1.18, 0.87),
            ),
            MinimumAdaptiveClearanceReservationAdjuster(1.6),
            RoundingAdaptiveClearanceReservationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 52492.9,
    )
}
