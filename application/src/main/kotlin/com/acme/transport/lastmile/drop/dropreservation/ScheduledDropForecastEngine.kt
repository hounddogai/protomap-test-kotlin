package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled drop forecast calculation. */
interface ScheduledDropForecastFactor {
    fun describe(): String
    fun adjust(item: ScheduledDropForecast, value: Double): Double
}

data class TieredScheduledDropForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDropForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDropForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledDropForecastAdjuster(var cap: Double) : ScheduledDropForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDropForecast, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledDropForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDropForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDropForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled drop forecast steps in order and keeps a trace of each. */
data class ScheduledDropForecastEngine(
    var steps: MutableList<ScheduledDropForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledDropForecast): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledDropForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledDropForecastEngine(flags: Registry): ScheduledDropForecastEngine {
    return ScheduledDropForecastEngine(
        steps = mutableListOf<ScheduledDropForecastFactor>(
            TieredScheduledDropForecastAdjuster(
                mutableListOf<Double>(1470.0, 5669.0, 6099.0, 6676.0),
                mutableListOf<Double>(1.11, 1.0, 0.85, 0.99),
            ),
            CapScheduledDropForecastAdjuster(33012.0),
            FlaggedScheduledDropForecastAdjuster(flags, "scheduled-drop-forecast-scheduled", 0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 50938.1,
    )
}
