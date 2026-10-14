package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual appointment milestone calculation. */
interface ManualAppointmentMilestoneStep {
    fun describe(): String
    fun adjust(item: ManualAppointmentMilestone, value: Double): Double
}

data class RoundingManualAppointmentMilestoneAdjuster(var step: Double) : ManualAppointmentMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAppointmentMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentManualAppointmentMilestoneAdjuster(var percent: Double) : ManualAppointmentMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAppointmentMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredManualAppointmentMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAppointmentMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAppointmentMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleManualAppointmentMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAppointmentMilestoneStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualAppointmentMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualAppointmentMilestoneAdjuster(var cap: Double) : ManualAppointmentMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAppointmentMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumManualAppointmentMilestoneAdjuster(var minimum: Double) : ManualAppointmentMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAppointmentMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual appointment milestone steps in order and keeps a trace of each. */
data class ManualAppointmentMilestoneEngine(
    var steps: MutableList<ManualAppointmentMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualAppointmentMilestone): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAppointmentMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualAppointmentMilestoneEngine(): ManualAppointmentMilestoneEngine {
    return ManualAppointmentMilestoneEngine(
        steps = mutableListOf<ManualAppointmentMilestoneStep>(
            RoundingManualAppointmentMilestoneAdjuster(0.25),
            PercentManualAppointmentMilestoneAdjuster(-17.5),
            TieredManualAppointmentMilestoneAdjuster(
                mutableListOf<Double>(4526.0, 5551.0, 6734.0),
                mutableListOf<Double>(1.13, 0.9, 0.91),
            ),
            TitleManualAppointmentMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 1.2, "online" to 1.34),
            ),
            CapManualAppointmentMilestoneAdjuster(45774.0),
            MinimumManualAppointmentMilestoneAdjuster(39.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 3823.8,
    )
}
