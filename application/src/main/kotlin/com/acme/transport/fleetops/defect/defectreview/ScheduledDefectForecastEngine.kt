package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled defect forecast calculation. */
interface ScheduledDefectForecastAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDefectForecast, value: Double): Double
}

data class ScheduledDefectForecastRetriesOffsetAdjuster(var weight: Double) : ScheduledDefectForecastAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ScheduledDefectForecast, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class MinimumScheduledDefectForecastAdjuster(var minimum: Double) : ScheduledDefectForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDefectForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledDefectForecastAdjuster(var step: Double) : ScheduledDefectForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDefectForecast, value: Double): Double = roundTo(value, step)
}

data class CapScheduledDefectForecastAdjuster(var cap: Double) : ScheduledDefectForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDefectForecast, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledDefectForecastAdjuster(var percent: Double) : ScheduledDefectForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDefectForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledDefectForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDefectForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDefectForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled defect forecast steps in order and keeps a trace of each. */
data class ScheduledDefectForecastEngine(
    var steps: MutableList<ScheduledDefectForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledDefectForecast): Double {
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

    fun evaluateAll(items: MutableList<ScheduledDefectForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledDefectForecastEngine(flags: Registry): ScheduledDefectForecastEngine {
    return ScheduledDefectForecastEngine(
        steps = mutableListOf<ScheduledDefectForecastAdjuster>(
            ScheduledDefectForecastRetriesOffsetAdjuster(0.94),
            MinimumScheduledDefectForecastAdjuster(8.4),
            RoundingScheduledDefectForecastAdjuster(0.1),
            CapScheduledDefectForecastAdjuster(18680.0),
            PercentScheduledDefectForecastAdjuster(-15.0),
            FlaggedScheduledDefectForecastAdjuster(flags, "scheduled-defect-forecast-pending", -5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 41579.3,
    )
}
