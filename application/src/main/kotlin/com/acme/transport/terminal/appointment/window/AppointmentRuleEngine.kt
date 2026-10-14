package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment rule calculation. */
interface AppointmentRuleAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentRule, value: Double): Double
}

data class CapAppointmentRuleAdjuster(var cap: Double) : AppointmentRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentRule, value: Double): Double = minOf(value, cap)
}

data class AppointmentRuleEndsAtOffsetAdjuster(var weight: Double) : AppointmentRuleAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: AppointmentRule, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class NameAppointmentRuleAdjuster(var factors: MutableMap<String, Double>) : AppointmentRuleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AppointmentRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAppointmentRuleAdjuster(var percent: Double) : AppointmentRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAppointmentRuleAdjuster(var minimum: Double) : AppointmentRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment rule steps in order and keeps a trace of each. */
data class AppointmentRuleEngine(
    var steps: MutableList<AppointmentRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentRule): Double {
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

    fun evaluateAll(items: MutableList<AppointmentRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentRuleEngine(): AppointmentRuleEngine {
    return AppointmentRuleEngine(
        steps = mutableListOf<AppointmentRuleAdjuster>(
            CapAppointmentRuleAdjuster(10883.0),
            AppointmentRuleEndsAtOffsetAdjuster(0.05),
            NameAppointmentRuleAdjuster(
                mutableMapOf<String, Double>("online" to 0.96, "east" to 1.4, "south" to 1.0),
            ),
            PercentAppointmentRuleAdjuster(-7.5),
            MinimumAppointmentRuleAdjuster(44.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 86549.2,
    )
}
