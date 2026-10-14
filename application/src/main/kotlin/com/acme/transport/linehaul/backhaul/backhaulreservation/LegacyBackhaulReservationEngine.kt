package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy backhaul reservation calculation. */
interface LegacyBackhaulReservationAdjuster {
    fun describe(): String
    fun adjust(item: LegacyBackhaulReservation, value: Double): Double
}

data class MinimumLegacyBackhaulReservationAdjuster(var minimum: Double) : LegacyBackhaulReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyBackhaulReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLegacyBackhaulReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyBackhaulReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyBackhaulReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyBackhaulReservationAdjuster(var step: Double) : LegacyBackhaulReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyBackhaulReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy backhaul reservation steps in order and keeps a trace of each. */
data class LegacyBackhaulReservationEngine(
    var steps: MutableList<LegacyBackhaulReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyBackhaulReservation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBackhaulReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyBackhaulReservationEngine(): LegacyBackhaulReservationEngine {
    return LegacyBackhaulReservationEngine(
        steps = mutableListOf<LegacyBackhaulReservationAdjuster>(
            MinimumLegacyBackhaulReservationAdjuster(6.5),
            TieredLegacyBackhaulReservationAdjuster(
                mutableListOf<Double>(685.0, 2803.0),
                mutableListOf<Double>(1.1, 0.84),
            ),
            RoundingLegacyBackhaulReservationAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 32072.3,
    )
}
