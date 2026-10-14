package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment rollup calculation. */
interface AppointmentRollupAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentRollup, value: Double): Double
}

data class MinimumAppointmentRollupAdjuster(var minimum: Double) : AppointmentRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameAppointmentRollupAdjuster(var factors: MutableMap<String, Double>) : AppointmentRollupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AppointmentRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AppointmentRollupFloorValueOffsetAdjuster(var weight: Double) : AppointmentRollupAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: AppointmentRollup, value: Double): Double = value + item.floorValue * weight
}

data class CapAppointmentRollupAdjuster(var cap: Double) : AppointmentRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentRollup, value: Double): Double = minOf(value, cap)
}

data class TieredAppointmentRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the appointment rollup steps in order and keeps a trace of each. */
data class AppointmentRollupEngine(
    var steps: MutableList<AppointmentRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AppointmentRollup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAppointmentRollupEngine(): AppointmentRollupEngine {
    return AppointmentRollupEngine(
        steps = mutableListOf<AppointmentRollupAdjuster>(
            MinimumAppointmentRollupAdjuster(23.2),
            NameAppointmentRollupAdjuster(
                mutableMapOf<String, Double>("online" to 1.21, "north" to 0.91, "central" to 1.04),
            ),
            AppointmentRollupFloorValueOffsetAdjuster(0.15),
            CapAppointmentRollupAdjuster(49822.0),
            TieredAppointmentRollupAdjuster(
                mutableListOf<Double>(1676.0, 5397.0, 6157.0, 8954.0),
                mutableListOf<Double>(1.07, 0.97, 1.06, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 16136.4,
    )
}
