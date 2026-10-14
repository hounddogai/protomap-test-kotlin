package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge reservation calculation. */
interface EvChargeReservationAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeReservation, value: Double): Double
}

data class RoundingEvChargeReservationAdjuster(var step: Double) : EvChargeReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeReservation, value: Double): Double = roundTo(value, step)
}

data class TieredEvChargeReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapEvChargeReservationAdjuster(var cap: Double) : EvChargeReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge reservation steps in order and keeps a trace of each. */
data class EvChargeReservationEngine(
    var steps: MutableList<EvChargeReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeReservation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeReservationEngine(): EvChargeReservationEngine {
    return EvChargeReservationEngine(
        steps = mutableListOf<EvChargeReservationAdjuster>(
            RoundingEvChargeReservationAdjuster(0.5),
            TieredEvChargeReservationAdjuster(
                mutableListOf<Double>(1442.0, 2786.0, 4061.0, 5217.0),
                mutableListOf<Double>(1.06, 1.0, 1.13, 1.04),
            ),
            CapEvChargeReservationAdjuster(34878.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 40892.3,
    )
}
