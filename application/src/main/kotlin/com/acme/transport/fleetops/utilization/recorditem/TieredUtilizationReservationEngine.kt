package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered utilization reservation calculation. */
interface TieredUtilizationReservationAdjuster {
    fun describe(): String
    fun adjust(item: TieredUtilizationReservation, value: Double): Double
}

data class CountryTieredUtilizationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredUtilizationReservationAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: TieredUtilizationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTieredUtilizationReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredUtilizationReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredUtilizationReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredUtilizationReservationAdjuster(
    var step: Double,
) : TieredUtilizationReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredUtilizationReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedTieredUtilizationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredUtilizationReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredUtilizationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered utilization reservation steps in order and keeps a trace of each. */
data class TieredUtilizationReservationEngine(
    var steps: MutableList<TieredUtilizationReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredUtilizationReservation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredUtilizationReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredUtilizationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredUtilizationReservationEngine(flags: Registry): TieredUtilizationReservationEngine {
    return TieredUtilizationReservationEngine(
        steps = mutableListOf<TieredUtilizationReservationAdjuster>(
            CountryTieredUtilizationReservationAdjuster(
                mutableMapOf<String, Double>("south" to 1.25, "retail" to 0.85, "east" to 1.03),
            ),
            TieredTieredUtilizationReservationAdjuster(
                mutableListOf<Double>(2408.0, 8837.0),
                mutableListOf<Double>(0.96, 0.99),
            ),
            RoundingTieredUtilizationReservationAdjuster(0.05),
            FlaggedTieredUtilizationReservationAdjuster(flags, "tiered-utilization-reservation-adaptive", 6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 97778.1,
    )
}
