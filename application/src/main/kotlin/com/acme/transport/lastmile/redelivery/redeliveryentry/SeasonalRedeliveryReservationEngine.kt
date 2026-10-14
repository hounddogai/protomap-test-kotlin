package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal redelivery reservation calculation. */
interface SeasonalRedeliveryReservationStep {
    fun describe(): String
    fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double
}

data class SeasonalRedeliveryReservationMarginOffsetAdjuster(
    var weight: Double,
) : SeasonalRedeliveryReservationStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double = value + item.margin * weight
}

data class MinimumSeasonalRedeliveryReservationAdjuster(
    var minimum: Double,
) : SeasonalRedeliveryReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalRedeliveryReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRedeliveryReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalRedeliveryReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRedeliveryReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalRedeliveryReservationAdjuster(var step: Double) : SeasonalRedeliveryReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalRedeliveryReservationAdjuster(
    var percent: Double,
) : SeasonalRedeliveryReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRedeliveryReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal redelivery reservation steps in order and keeps a trace of each. */
data class SeasonalRedeliveryReservationEngine(
    var steps: MutableList<SeasonalRedeliveryReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalRedeliveryReservation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRedeliveryReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRedeliveryReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalRedeliveryReservationEngine(flags: Registry): SeasonalRedeliveryReservationEngine {
    return SeasonalRedeliveryReservationEngine(
        steps = mutableListOf<SeasonalRedeliveryReservationStep>(
            SeasonalRedeliveryReservationMarginOffsetAdjuster(1.56),
            MinimumSeasonalRedeliveryReservationAdjuster(29.5),
            FlaggedSeasonalRedeliveryReservationAdjuster(flags, "seasonal-redelivery-reservation-batched", 2.3),
            TieredSeasonalRedeliveryReservationAdjuster(
                mutableListOf<Double>(1603.0, 8405.0),
                mutableListOf<Double>(0.98, 1.13),
            ),
            RoundingSeasonalRedeliveryReservationAdjuster(0.25),
            PercentSeasonalRedeliveryReservationAdjuster(-16.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 40792.5,
    )
}
