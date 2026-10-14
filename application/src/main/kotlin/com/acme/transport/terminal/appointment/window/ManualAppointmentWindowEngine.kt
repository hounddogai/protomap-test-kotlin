package com.acme.transport.terminal.appointment.window

/** One step of the manual appointment window calculation. */
interface ManualAppointmentWindowStep {
    fun describe(): String
    fun adjust(item: ManualAppointmentWindow, value: Double): Double
}

data class TieredManualAppointmentWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAppointmentWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAppointmentWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualAppointmentWindowAdjuster(var percent: Double) : ManualAppointmentWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAppointmentWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualAppointmentWindowMinimumOffsetAdjuster(var weight: Double) : ManualAppointmentWindowStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ManualAppointmentWindow, value: Double): Double = value + item.minimum * weight
}

/** Runs the manual appointment window steps in order and keeps a trace of each. */
data class ManualAppointmentWindowEngine(
    var steps: MutableList<ManualAppointmentWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualAppointmentWindow): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAppointmentWindowStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAppointmentWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualAppointmentWindowEngine(): ManualAppointmentWindowEngine {
    return ManualAppointmentWindowEngine(
        steps = mutableListOf<ManualAppointmentWindowStep>(
            TieredManualAppointmentWindowAdjuster(
                mutableListOf<Double>(86.0, 7253.0, 8912.0),
                mutableListOf<Double>(0.84, 0.93, 0.86),
            ),
            PercentManualAppointmentWindowAdjuster(9.4),
            ManualAppointmentWindowMinimumOffsetAdjuster(1.64),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 64096.7,
    )
}
