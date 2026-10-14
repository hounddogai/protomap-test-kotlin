package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending scorecard forecast calculation. */
interface PendingScorecardForecastModifier {
    fun describe(): String
    fun adjust(item: PendingScorecardForecast, value: Double): Double
}

data class RoundingPendingScorecardForecastAdjuster(var step: Double) : PendingScorecardForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingScorecardForecast, value: Double): Double = roundTo(value, step)
}

data class TieredPendingScorecardForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingScorecardForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingScorecardForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingScorecardForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingScorecardForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingScorecardForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingScorecardForecastAdjuster(var cap: Double) : PendingScorecardForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingScorecardForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the pending scorecard forecast steps in order and keeps a trace of each. */
data class PendingScorecardForecastEngine(
    var steps: MutableList<PendingScorecardForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingScorecardForecast): Double {
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

    fun evaluateAll(items: MutableList<PendingScorecardForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingScorecardForecastEngine(flags: Registry): PendingScorecardForecastEngine {
    return PendingScorecardForecastEngine(
        steps = mutableListOf<PendingScorecardForecastModifier>(
            RoundingPendingScorecardForecastAdjuster(0.05),
            TieredPendingScorecardForecastAdjuster(
                mutableListOf<Double>(636.0, 2815.0),
                mutableListOf<Double>(1.02, 0.87),
            ),
            FlaggedPendingScorecardForecastAdjuster(flags, "pending-scorecard-forecast-legacy", -6.8),
            CapPendingScorecardForecastAdjuster(37085.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 93771.2,
    )
}
