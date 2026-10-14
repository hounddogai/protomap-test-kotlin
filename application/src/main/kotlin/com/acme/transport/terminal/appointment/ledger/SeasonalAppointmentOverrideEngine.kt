package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal appointment override calculation. */
interface SeasonalAppointmentOverrideAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalAppointmentOverride, value: Double): Double
}

data class RoundingSeasonalAppointmentOverrideAdjuster(var step: Double) : SeasonalAppointmentOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAppointmentOverride, value: Double): Double = roundTo(value, step)
}

data class SeasonalAppointmentOverrideAmountOffsetAdjuster(
    var weight: Double,
) : SeasonalAppointmentOverrideAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: SeasonalAppointmentOverride, value: Double): Double = value + item.amount * weight
}

data class PercentSeasonalAppointmentOverrideAdjuster(
    var percent: Double,
) : SeasonalAppointmentOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAppointmentOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalAppointmentOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAppointmentOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAppointmentOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalAppointmentOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalAppointmentOverrideAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalAppointmentOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal appointment override steps in order and keeps a trace of each. */
data class SeasonalAppointmentOverrideEngine(
    var steps: MutableList<SeasonalAppointmentOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalAppointmentOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAppointmentOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAppointmentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalAppointmentOverrideEngine(): SeasonalAppointmentOverrideEngine {
    return SeasonalAppointmentOverrideEngine(
        steps = mutableListOf<SeasonalAppointmentOverrideAdjuster>(
            RoundingSeasonalAppointmentOverrideAdjuster(0.5),
            SeasonalAppointmentOverrideAmountOffsetAdjuster(1.74),
            PercentSeasonalAppointmentOverrideAdjuster(-0.3),
            TieredSeasonalAppointmentOverrideAdjuster(
                mutableListOf<Double>(3408.0, 8163.0),
                mutableListOf<Double>(0.81, 1.11),
            ),
            LabelSeasonalAppointmentOverrideAdjuster(
                mutableMapOf<String, Double>("retail" to 1.19, "south" to 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 94341.0,
    )
}
