package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled hold forecast calculation. */
interface ScheduledHoldForecastStep {
    fun describe(): String
    fun adjust(item: ScheduledHoldForecast, value: Double): Double
}

data class PercentScheduledHoldForecastAdjuster(var percent: Double) : ScheduledHoldForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledHoldForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingScheduledHoldForecastAdjuster(var step: Double) : ScheduledHoldForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledHoldForecast, value: Double): Double = roundTo(value, step)
}

data class LaneScheduledHoldForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledHoldForecastStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledHoldForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledHoldForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHoldForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHoldForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled hold forecast steps in order and keeps a trace of each. */
data class ScheduledHoldForecastEngine(
    var steps: MutableList<ScheduledHoldForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledHoldForecast): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledHoldForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledHoldForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledHoldForecastEngine(flags: Registry): ScheduledHoldForecastEngine {
    return ScheduledHoldForecastEngine(
        steps = mutableListOf<ScheduledHoldForecastStep>(
            PercentScheduledHoldForecastAdjuster(-0.2),
            RoundingScheduledHoldForecastAdjuster(1.0),
            LaneScheduledHoldForecastAdjuster(mutableMapOf<String, Double>("central" to 1.21, "west" to 0.79)),
            FlaggedScheduledHoldForecastAdjuster(flags, "scheduled-hold-forecast-strict", -3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 38920.9,
    )
}
