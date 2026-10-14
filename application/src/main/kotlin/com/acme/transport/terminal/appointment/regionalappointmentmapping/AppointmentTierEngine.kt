package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment tier calculation. */
interface AppointmentTierModifier {
    fun describe(): String
    fun adjust(item: AppointmentTier, value: Double): Double
}

data class TieredAppointmentTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotAppointmentTierAdjuster(var factors: MutableMap<String, Double>) : AppointmentTierModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: AppointmentTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAppointmentTierAdjuster(var cap: Double) : AppointmentTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentTier, value: Double): Double = minOf(value, cap)
}

data class MinimumAppointmentTierAdjuster(var minimum: Double) : AppointmentTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment tier steps in order and keeps a trace of each. */
data class AppointmentTierEngine(
    var steps: MutableList<AppointmentTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentTier): Double {
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

    fun evaluateAll(items: MutableList<AppointmentTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentTierEngine(): AppointmentTierEngine {
    return AppointmentTierEngine(
        steps = mutableListOf<AppointmentTierModifier>(
            TieredAppointmentTierAdjuster(
                mutableListOf<Double>(1738.0, 4017.0),
                mutableListOf<Double>(1.06, 0.95),
            ),
            DepotAppointmentTierAdjuster(mutableMapOf<String, Double>("online" to 1.0)),
            CapAppointmentTierAdjuster(41947.0),
            MinimumAppointmentTierAdjuster(46.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 76967.6,
    )
}
