package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the hub schedule calculation. */
interface HubScheduleFactor {
    fun describe(): String
    fun adjust(item: HubSchedule, value: Double): Double
}

data class RoundingHubScheduleAdjuster(var step: Double) : HubScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumHubScheduleAdjuster(var minimum: Double) : HubScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentHubScheduleAdjuster(var percent: Double) : HubScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the hub schedule steps in order and keeps a trace of each. */
data class HubScheduleEngine(
    var steps: MutableList<HubScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HubSchedule): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HubSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHubScheduleEngine(): HubScheduleEngine {
    return HubScheduleEngine(
        steps = mutableListOf<HubScheduleFactor>(
            RoundingHubScheduleAdjuster(1.0),
            MinimumHubScheduleAdjuster(29.4),
            PercentHubScheduleAdjuster(3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 77075.6,
    )
}
