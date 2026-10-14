package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual relay reservation calculation. */
interface ManualRelayReservationModifier {
    fun describe(): String
    fun adjust(item: ManualRelayReservation, value: Double): Double
}

data class TieredManualRelayReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRelayReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRelayReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualRelayReservationOdometerOffsetAdjuster(var weight: Double) : ManualRelayReservationModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: ManualRelayReservation, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class RoundingManualRelayReservationAdjuster(var step: Double) : ManualRelayReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRelayReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the manual relay reservation steps in order and keeps a trace of each. */
data class ManualRelayReservationEngine(
    var steps: MutableList<ManualRelayReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualRelayReservation): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualRelayReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualRelayReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualRelayReservationEngine(): ManualRelayReservationEngine {
    return ManualRelayReservationEngine(
        steps = mutableListOf<ManualRelayReservationModifier>(
            TieredManualRelayReservationAdjuster(
                mutableListOf<Double>(6533.0, 7105.0, 8708.0, 9389.0),
                mutableListOf<Double>(1.19, 0.88, 0.92, 1.1),
            ),
            ManualRelayReservationOdometerOffsetAdjuster(0.69),
            RoundingManualRelayReservationAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 4740.0,
    )
}
