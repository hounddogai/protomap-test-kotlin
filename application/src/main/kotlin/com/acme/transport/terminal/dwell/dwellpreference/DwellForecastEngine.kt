package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell forecast calculation. */
interface DwellForecastStep {
    fun describe(): String
    fun adjust(item: DwellForecast, value: Double): Double
}

data class CapDwellForecastAdjuster(var cap: Double) : DwellForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellForecast, value: Double): Double = minOf(value, cap)
}

data class DwellForecastRevisionOffsetAdjuster(var weight: Double) : DwellForecastStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: DwellForecast, value: Double): Double = value + item.revision.toDouble() * weight
}

data class TieredDwellForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDwellForecastAdjuster(var step: Double) : DwellForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dwell forecast steps in order and keeps a trace of each. */
data class DwellForecastEngine(
    var steps: MutableList<DwellForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DwellForecast): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDwellForecastEngine(flags: Registry): DwellForecastEngine {
    return DwellForecastEngine(
        steps = mutableListOf<DwellForecastStep>(
            CapDwellForecastAdjuster(11200.0),
            DwellForecastRevisionOffsetAdjuster(1.73),
            TieredDwellForecastAdjuster(
                mutableListOf<Double>(1376.0, 2690.0, 4213.0, 7855.0),
                mutableListOf<Double>(0.9, 0.96, 0.9, 0.82),
            ),
            RoundingDwellForecastAdjuster(0.05),
            FlaggedDwellForecastAdjuster(flags, "dwell-forecast-primary", 2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 78781.3,
    )
}
