package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.numbers.roundTo

/** One step of the default appointment score calculation. */
interface DefaultAppointmentScoreFactor {
    fun describe(): String
    fun adjust(item: DefaultAppointmentScore, value: Double): Double
}

data class TieredDefaultAppointmentScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAppointmentScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAppointmentScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultAppointmentScoreAdjuster(var step: Double) : DefaultAppointmentScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAppointmentScore, value: Double): Double = roundTo(value, step)
}

data class DefaultAppointmentScoreEtaMinutesOffsetAdjuster(var weight: Double) : DefaultAppointmentScoreFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DefaultAppointmentScore, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the default appointment score steps in order and keeps a trace of each. */
data class DefaultAppointmentScoreEngine(
    var steps: MutableList<DefaultAppointmentScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultAppointmentScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAppointmentScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAppointmentScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultAppointmentScoreEngine(): DefaultAppointmentScoreEngine {
    return DefaultAppointmentScoreEngine(
        steps = mutableListOf<DefaultAppointmentScoreFactor>(
            TieredDefaultAppointmentScoreAdjuster(
                mutableListOf<Double>(4912.0, 6958.0, 8025.0),
                mutableListOf<Double>(0.99, 1.04, 1.15),
            ),
            RoundingDefaultAppointmentScoreAdjuster(5.0),
            DefaultAppointmentScoreEtaMinutesOffsetAdjuster(1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 6984.3,
    )
}
