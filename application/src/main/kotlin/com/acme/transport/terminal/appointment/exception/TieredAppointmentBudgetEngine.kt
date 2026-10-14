package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered appointment budget calculation. */
interface TieredAppointmentBudgetStep {
    fun describe(): String
    fun adjust(item: TieredAppointmentBudget, value: Double): Double
}

data class NameTieredAppointmentBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAppointmentBudgetStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredAppointmentBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredAppointmentBudgetAdjuster(var step: Double) : TieredAppointmentBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredAppointmentBudget, value: Double): Double = roundTo(value, step)
}

data class PercentTieredAppointmentBudgetAdjuster(var percent: Double) : TieredAppointmentBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAppointmentBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredAppointmentBudgetAdjuster(var cap: Double) : TieredAppointmentBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAppointmentBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredAppointmentBudgetAdjuster(var minimum: Double) : TieredAppointmentBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredAppointmentBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredAppointmentBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAppointmentBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAppointmentBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered appointment budget steps in order and keeps a trace of each. */
data class TieredAppointmentBudgetEngine(
    var steps: MutableList<TieredAppointmentBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredAppointmentBudget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAppointmentBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredAppointmentBudgetEngine(flags: Registry): TieredAppointmentBudgetEngine {
    return TieredAppointmentBudgetEngine(
        steps = mutableListOf<TieredAppointmentBudgetStep>(
            NameTieredAppointmentBudgetAdjuster(mutableMapOf<String, Double>("retail" to 0.8, "west" to 1.25)),
            RoundingTieredAppointmentBudgetAdjuster(0.05),
            PercentTieredAppointmentBudgetAdjuster(14.0),
            CapTieredAppointmentBudgetAdjuster(47331.0),
            MinimumTieredAppointmentBudgetAdjuster(31.7),
            FlaggedTieredAppointmentBudgetAdjuster(flags, "tiered-appointment-budget-global", -0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 46082.9,
    )
}
