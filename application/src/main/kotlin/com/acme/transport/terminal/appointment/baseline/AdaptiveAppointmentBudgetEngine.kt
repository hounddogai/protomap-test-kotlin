package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive appointment budget calculation. */
interface AdaptiveAppointmentBudgetStep {
    fun describe(): String
    fun adjust(item: AdaptiveAppointmentBudget, value: Double): Double
}

data class FlaggedAdaptiveAppointmentBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAppointmentBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAppointmentBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveAppointmentBudgetAdjuster(var step: Double) : AdaptiveAppointmentBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAppointmentBudget, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveAppointmentBudgetAdjuster(var minimum: Double) : AdaptiveAppointmentBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAppointmentBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveAppointmentBudgetAdjuster(var percent: Double) : AdaptiveAppointmentBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAppointmentBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveAppointmentBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAppointmentBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAppointmentBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive appointment budget steps in order and keeps a trace of each. */
data class AdaptiveAppointmentBudgetEngine(
    var steps: MutableList<AdaptiveAppointmentBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveAppointmentBudget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAppointmentBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveAppointmentBudgetEngine(flags: Registry): AdaptiveAppointmentBudgetEngine {
    return AdaptiveAppointmentBudgetEngine(
        steps = mutableListOf<AdaptiveAppointmentBudgetStep>(
            FlaggedAdaptiveAppointmentBudgetAdjuster(flags, "adaptive-appointment-budget-rolling", -6.1),
            RoundingAdaptiveAppointmentBudgetAdjuster(0.5),
            MinimumAdaptiveAppointmentBudgetAdjuster(44.8),
            PercentAdaptiveAppointmentBudgetAdjuster(1.0),
            TieredAdaptiveAppointmentBudgetAdjuster(
                mutableListOf<Double>(1733.0, 4220.0, 4916.0),
                mutableListOf<Double>(1.01, 1.16, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 41804.8,
    )
}
