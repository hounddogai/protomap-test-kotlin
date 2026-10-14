package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional idling reservation calculation. */
interface RegionalIdlingReservationStep {
    fun describe(): String
    fun adjust(item: RegionalIdlingReservation, value: Double): Double
}

data class MinimumRegionalIdlingReservationAdjuster(var minimum: Double) : RegionalIdlingReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalIdlingReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalIdlingReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalIdlingReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalIdlingReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalIdlingReservationAdjuster(var cap: Double) : RegionalIdlingReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalIdlingReservation, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalIdlingReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIdlingReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIdlingReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleRegionalIdlingReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalIdlingReservationStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RegionalIdlingReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalIdlingReservationAdjuster(var step: Double) : RegionalIdlingReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalIdlingReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the regional idling reservation steps in order and keeps a trace of each. */
data class RegionalIdlingReservationEngine(
    var steps: MutableList<RegionalIdlingReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalIdlingReservation): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIdlingReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIdlingReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalIdlingReservationEngine(flags: Registry): RegionalIdlingReservationEngine {
    return RegionalIdlingReservationEngine(
        steps = mutableListOf<RegionalIdlingReservationStep>(
            MinimumRegionalIdlingReservationAdjuster(5.7),
            TieredRegionalIdlingReservationAdjuster(
                mutableListOf<Double>(298.0, 1242.0, 5648.0),
                mutableListOf<Double>(1.18, 1.0, 1.2),
            ),
            CapRegionalIdlingReservationAdjuster(15716.0),
            FlaggedRegionalIdlingReservationAdjuster(flags, "regional-idling-reservation-capped", -6.4),
            LocaleRegionalIdlingReservationAdjuster(
                mutableMapOf<String, Double>("online" to 1.17, "south" to 0.85, "north" to 1.02),
            ),
            RoundingRegionalIdlingReservationAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 72618.0,
    )
}
