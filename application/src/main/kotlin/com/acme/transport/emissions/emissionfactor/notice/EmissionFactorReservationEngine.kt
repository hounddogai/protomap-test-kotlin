package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.flags.Registry

/** One step of the emission factor reservation calculation. */
interface EmissionFactorReservationModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorReservation, value: Double): Double
}

data class PercentEmissionFactorReservationAdjuster(var percent: Double) : EmissionFactorReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredEmissionFactorReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class EmissionFactorReservationPriorityOffsetAdjuster(
    var weight: Double,
) : EmissionFactorReservationModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: EmissionFactorReservation, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class MinimumEmissionFactorReservationAdjuster(var minimum: Double) : EmissionFactorReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEmissionFactorReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionEmissionFactorReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorReservationModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: EmissionFactorReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor reservation steps in order and keeps a trace of each. */
data class EmissionFactorReservationEngine(
    var steps: MutableList<EmissionFactorReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorReservationEngine(flags: Registry): EmissionFactorReservationEngine {
    return EmissionFactorReservationEngine(
        steps = mutableListOf<EmissionFactorReservationModifier>(
            PercentEmissionFactorReservationAdjuster(23.6),
            TieredEmissionFactorReservationAdjuster(
                mutableListOf<Double>(348.0, 829.0, 5147.0, 9967.0),
                mutableListOf<Double>(0.81, 0.93, 0.96, 1.1),
            ),
            EmissionFactorReservationPriorityOffsetAdjuster(0.92),
            MinimumEmissionFactorReservationAdjuster(43.6),
            FlaggedEmissionFactorReservationAdjuster(flags, "emission-factor-reservation-rolling", -3.4),
            RegionEmissionFactorReservationAdjuster(mutableMapOf<String, Double>("south" to 1.01)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 5029.3,
    )
}
