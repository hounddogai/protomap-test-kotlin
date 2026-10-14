package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment slot calculation. */
interface AppointmentSlotModifier {
    fun describe(): String
    fun adjust(item: AppointmentSlot, value: Double): Double
}

data class CapAppointmentSlotAdjuster(var cap: Double) : AppointmentSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentSlot, value: Double): Double = minOf(value, cap)
}

data class PercentAppointmentSlotAdjuster(var percent: Double) : AppointmentSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAppointmentSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AppointmentSlotCreatedAtOffsetAdjuster(var weight: Double) : AppointmentSlotModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: AppointmentSlot, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class MinimumAppointmentSlotAdjuster(var minimum: Double) : AppointmentSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment slot steps in order and keeps a trace of each. */
data class AppointmentSlotEngine(
    var steps: MutableList<AppointmentSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentSlotEngine(): AppointmentSlotEngine {
    return AppointmentSlotEngine(
        steps = mutableListOf<AppointmentSlotModifier>(
            CapAppointmentSlotAdjuster(47956.0),
            PercentAppointmentSlotAdjuster(-24.8),
            TieredAppointmentSlotAdjuster(
                mutableListOf<Double>(5092.0, 7702.0),
                mutableListOf<Double>(1.04, 0.9),
            ),
            AppointmentSlotCreatedAtOffsetAdjuster(1.98),
            MinimumAppointmentSlotAdjuster(17.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 17272.6,
    )
}
