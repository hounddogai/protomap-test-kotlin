package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled emission factor reservation calculation. */
interface ScheduledEmissionFactorReservationAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledEmissionFactorReservation, value: Double): Double
}

data class TieredScheduledEmissionFactorReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledEmissionFactorReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledEmissionFactorReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledEmissionFactorReservationAdjuster(
    var step: Double,
) : ScheduledEmissionFactorReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledEmissionFactorReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledEmissionFactorReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledEmissionFactorReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledEmissionFactorReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledEmissionFactorReservationAdjuster(
    var percent: Double,
) : ScheduledEmissionFactorReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledEmissionFactorReservation, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled emission factor reservation steps in order and keeps a trace of each. */
data class ScheduledEmissionFactorReservationEngine(
    var steps: MutableList<ScheduledEmissionFactorReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledEmissionFactorReservation): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledEmissionFactorReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledEmissionFactorReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledEmissionFactorReservationEngine(flags: Registry): ScheduledEmissionFactorReservationEngine {
    return ScheduledEmissionFactorReservationEngine(
        steps = mutableListOf<ScheduledEmissionFactorReservationAdjuster>(
            TieredScheduledEmissionFactorReservationAdjuster(
                mutableListOf<Double>(664.0, 8403.0),
                mutableListOf<Double>(1.19, 1.05),
            ),
            RoundingScheduledEmissionFactorReservationAdjuster(1.0),
            FlaggedScheduledEmissionFactorReservationAdjuster(
                flags,
                "scheduled-emission-factor-reservation-weighted",
                -6.8,
            ),
            PercentScheduledEmissionFactorReservationAdjuster(-4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 91264.0,
    )
}
