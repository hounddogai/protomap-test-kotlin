package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive appointment cycle calculation. */
interface AdaptiveAppointmentCycleStep {
    fun describe(): String
    fun adjust(item: AdaptiveAppointmentCycle, value: Double): Double
}

data class PercentAdaptiveAppointmentCycleAdjuster(var percent: Double) : AdaptiveAppointmentCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAppointmentCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveAppointmentCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAppointmentCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAppointmentCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryAdaptiveAppointmentCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAppointmentCycleStep {
    override fun describe(): String = "by country"

    override fun adjust(item: AdaptiveAppointmentCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAdaptiveAppointmentCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAppointmentCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAppointmentCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveAppointmentCycleAdjuster(var cap: Double) : AdaptiveAppointmentCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAppointmentCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive appointment cycle steps in order and keeps a trace of each. */
data class AdaptiveAppointmentCycleEngine(
    var steps: MutableList<AdaptiveAppointmentCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveAppointmentCycle): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveAppointmentCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveAppointmentCycleEngine(flags: Registry): AdaptiveAppointmentCycleEngine {
    return AdaptiveAppointmentCycleEngine(
        steps = mutableListOf<AdaptiveAppointmentCycleStep>(
            PercentAdaptiveAppointmentCycleAdjuster(14.4),
            TieredAdaptiveAppointmentCycleAdjuster(
                mutableListOf<Double>(46.0, 5737.0, 6230.0),
                mutableListOf<Double>(0.97, 0.81, 1.14),
            ),
            CountryAdaptiveAppointmentCycleAdjuster(
                mutableMapOf<String, Double>("retail" to 0.94, "south" to 1.25, "north" to 0.73),
            ),
            FlaggedAdaptiveAppointmentCycleAdjuster(flags, "adaptive-appointment-cycle-batched", 9.0),
            CapAdaptiveAppointmentCycleAdjuster(33976.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 57242.2,
    )
}
