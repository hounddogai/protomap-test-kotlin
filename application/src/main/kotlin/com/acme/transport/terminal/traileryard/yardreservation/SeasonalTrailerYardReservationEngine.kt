package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal trailer yard reservation calculation. */
interface SeasonalTrailerYardReservationFactor {
    fun describe(): String
    fun adjust(item: SeasonalTrailerYardReservation, value: Double): Double
}

data class PercentSeasonalTrailerYardReservationAdjuster(
    var percent: Double,
) : SeasonalTrailerYardReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTrailerYardReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalTrailerYardReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTrailerYardReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTrailerYardReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalTrailerYardReservationAdjuster(
    var minimum: Double,
) : SeasonalTrailerYardReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTrailerYardReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal trailer yard reservation steps in order and keeps a trace of each. */
data class SeasonalTrailerYardReservationEngine(
    var steps: MutableList<SeasonalTrailerYardReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalTrailerYardReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTrailerYardReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTrailerYardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerYardReservationEngine(flags: Registry): SeasonalTrailerYardReservationEngine {
    return SeasonalTrailerYardReservationEngine(
        steps = mutableListOf<SeasonalTrailerYardReservationFactor>(
            PercentSeasonalTrailerYardReservationAdjuster(-19.2),
            FlaggedSeasonalTrailerYardReservationAdjuster(
                flags,
                "seasonal-trailer-yard-reservation-tiered",
                7.9,
            ),
            MinimumSeasonalTrailerYardReservationAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 15205.5,
    )
}
