package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute reservation calculation. */
interface DisputeReservationFactor {
    fun describe(): String
    fun adjust(item: DisputeReservation, value: Double): Double
}

data class MinimumDisputeReservationAdjuster(var minimum: Double) : DisputeReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DisputeReservationDiscountOffsetAdjuster(var weight: Double) : DisputeReservationFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DisputeReservation, value: Double): Double = value + item.discount * weight
}

data class RoundingDisputeReservationAdjuster(var step: Double) : DisputeReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeReservation, value: Double): Double = roundTo(value, step)
}

data class TieredDisputeReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDisputeReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dispute reservation steps in order and keeps a trace of each. */
data class DisputeReservationEngine(
    var steps: MutableList<DisputeReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DisputeReservation): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDisputeReservationEngine(flags: Registry): DisputeReservationEngine {
    return DisputeReservationEngine(
        steps = mutableListOf<DisputeReservationFactor>(
            MinimumDisputeReservationAdjuster(33.0),
            DisputeReservationDiscountOffsetAdjuster(1.82),
            RoundingDisputeReservationAdjuster(0.5),
            TieredDisputeReservationAdjuster(
                mutableListOf<Double>(5037.0, 7351.0, 7451.0),
                mutableListOf<Double>(0.93, 0.88, 0.84),
            ),
            FlaggedDisputeReservationAdjuster(flags, "dispute-reservation-primary", -9.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 76950.7,
    )
}
