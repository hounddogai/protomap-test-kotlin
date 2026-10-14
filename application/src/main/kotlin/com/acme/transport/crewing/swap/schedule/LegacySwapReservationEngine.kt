package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy swap reservation calculation. */
interface LegacySwapReservationAdjuster {
    fun describe(): String
    fun adjust(item: LegacySwapReservation, value: Double): Double
}

data class RoundingLegacySwapReservationAdjuster(var step: Double) : LegacySwapReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacySwapReservation, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacySwapReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacySwapReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacySwapReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacySwapReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacySwapReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacySwapReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy swap reservation steps in order and keeps a trace of each. */
data class LegacySwapReservationEngine(
    var steps: MutableList<LegacySwapReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacySwapReservation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacySwapReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacySwapReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacySwapReservationEngine(flags: Registry): LegacySwapReservationEngine {
    return LegacySwapReservationEngine(
        steps = mutableListOf<LegacySwapReservationAdjuster>(
            RoundingLegacySwapReservationAdjuster(0.1),
            FlaggedLegacySwapReservationAdjuster(flags, "legacy-swap-reservation-legacy", -4.8),
            TieredLegacySwapReservationAdjuster(
                mutableListOf<Double>(1830.0, 5241.0, 8118.0, 8597.0),
                mutableListOf<Double>(1.07, 1.06, 0.91, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 90103.1,
    )
}
