package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment estimate calculation. */
interface AppointmentEstimateStep {
    fun describe(): String
    fun adjust(item: AppointmentEstimate, value: Double): Double
}

data class CapAppointmentEstimateAdjuster(var cap: Double) : AppointmentEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentEstimate, value: Double): Double = minOf(value, cap)
}

data class CategoryAppointmentEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentEstimateStep {
    override fun describe(): String = "by category"

    override fun adjust(item: AppointmentEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAppointmentEstimateAdjuster(var minimum: Double) : AppointmentEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment estimate steps in order and keeps a trace of each. */
data class AppointmentEstimateEngine(
    var steps: MutableList<AppointmentEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AppointmentEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAppointmentEstimateEngine(): AppointmentEstimateEngine {
    return AppointmentEstimateEngine(
        steps = mutableListOf<AppointmentEstimateStep>(
            CapAppointmentEstimateAdjuster(10654.0),
            CategoryAppointmentEstimateAdjuster(mutableMapOf<String, Double>("north" to 1.24)),
            MinimumAppointmentEstimateAdjuster(41.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 37849.1,
    )
}
