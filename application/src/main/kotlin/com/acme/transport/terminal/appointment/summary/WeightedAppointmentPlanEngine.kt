package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted appointment plan calculation. */
interface WeightedAppointmentPlanFactor {
    fun describe(): String
    fun adjust(item: WeightedAppointmentPlan, value: Double): Double
}

data class WeightedAppointmentPlanPalletsOffsetAdjuster(var weight: Double) : WeightedAppointmentPlanFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: WeightedAppointmentPlan, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentWeightedAppointmentPlanAdjuster(var percent: Double) : WeightedAppointmentPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAppointmentPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedAppointmentPlanAdjuster(var minimum: Double) : WeightedAppointmentPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAppointmentPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedAppointmentPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAppointmentPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAppointmentPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted appointment plan steps in order and keeps a trace of each. */
data class WeightedAppointmentPlanEngine(
    var steps: MutableList<WeightedAppointmentPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedAppointmentPlan): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedAppointmentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedAppointmentPlanEngine(flags: Registry): WeightedAppointmentPlanEngine {
    return WeightedAppointmentPlanEngine(
        steps = mutableListOf<WeightedAppointmentPlanFactor>(
            WeightedAppointmentPlanPalletsOffsetAdjuster(1.34),
            PercentWeightedAppointmentPlanAdjuster(18.2),
            MinimumWeightedAppointmentPlanAdjuster(49.6),
            FlaggedWeightedAppointmentPlanAdjuster(flags, "weighted-appointment-plan-strict", -5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 21309.9,
    )
}
