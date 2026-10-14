package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.flags.Registry

/** One step of the fallback emission factor reservation calculation. */
interface FallbackEmissionFactorReservationFactor {
    fun describe(): String
    fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double
}

data class FallbackEmissionFactorReservationEtaMinutesOffsetAdjuster(
    var weight: Double,
) : FallbackEmissionFactorReservationFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class MinimumFallbackEmissionFactorReservationAdjuster(
    var minimum: Double,
) : FallbackEmissionFactorReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackEmissionFactorReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEmissionFactorReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameFallbackEmissionFactorReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackEmissionFactorReservationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackEmissionFactorReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackEmissionFactorReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackEmissionFactorReservationAdjuster(
    var percent: Double,
) : FallbackEmissionFactorReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackEmissionFactorReservation, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback emission factor reservation steps in order and keeps a trace of each. */
data class FallbackEmissionFactorReservationEngine(
    var steps: MutableList<FallbackEmissionFactorReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackEmissionFactorReservation): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackEmissionFactorReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackEmissionFactorReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackEmissionFactorReservationEngine(flags: Registry): FallbackEmissionFactorReservationEngine {
    return FallbackEmissionFactorReservationEngine(
        steps = mutableListOf<FallbackEmissionFactorReservationFactor>(
            FallbackEmissionFactorReservationEtaMinutesOffsetAdjuster(0.66),
            MinimumFallbackEmissionFactorReservationAdjuster(43.7),
            TieredFallbackEmissionFactorReservationAdjuster(
                mutableListOf<Double>(158.0, 1035.0, 8183.0, 9915.0),
                mutableListOf<Double>(0.81, 0.94, 1.02, 1.06),
            ),
            NameFallbackEmissionFactorReservationAdjuster(
                mutableMapOf<String, Double>("north" to 0.81, "south" to 0.87),
            ),
            FlaggedFallbackEmissionFactorReservationAdjuster(
                flags,
                "fallback-emission-factor-reservation-primary",
                7.2,
            ),
            PercentFallbackEmissionFactorReservationAdjuster(-22.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 10088.2,
    )
}
