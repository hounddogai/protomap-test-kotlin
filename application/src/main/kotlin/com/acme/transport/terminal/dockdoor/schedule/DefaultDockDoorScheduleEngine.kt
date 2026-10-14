package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the default dock door schedule calculation. */
interface DefaultDockDoorScheduleStep {
    fun describe(): String
    fun adjust(item: DefaultDockDoorSchedule, value: Double): Double
}

data class RoundingDefaultDockDoorScheduleAdjuster(var step: Double) : DefaultDockDoorScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDockDoorSchedule, value: Double): Double = roundTo(value, step)
}

data class NameDefaultDockDoorScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDockDoorScheduleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultDockDoorSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefaultDockDoorScheduleWindowDaysOffsetAdjuster(var weight: Double) : DefaultDockDoorScheduleStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: DefaultDockDoorSchedule, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentDefaultDockDoorScheduleAdjuster(var percent: Double) : DefaultDockDoorScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDockDoorSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default dock door schedule steps in order and keeps a trace of each. */
data class DefaultDockDoorScheduleEngine(
    var steps: MutableList<DefaultDockDoorScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultDockDoorSchedule): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDockDoorScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDockDoorSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultDockDoorScheduleEngine(): DefaultDockDoorScheduleEngine {
    return DefaultDockDoorScheduleEngine(
        steps = mutableListOf<DefaultDockDoorScheduleStep>(
            RoundingDefaultDockDoorScheduleAdjuster(1.0),
            NameDefaultDockDoorScheduleAdjuster(mutableMapOf<String, Double>("online" to 0.72)),
            DefaultDockDoorScheduleWindowDaysOffsetAdjuster(0.97),
            PercentDefaultDockDoorScheduleAdjuster(-5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 42090.7,
    )
}
