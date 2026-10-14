package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary declaration forecast calculation. */
interface PrimaryDeclarationForecastStep {
    fun describe(): String
    fun adjust(item: PrimaryDeclarationForecast, value: Double): Double
}

data class FlaggedPrimaryDeclarationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDeclarationForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDeclarationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryDeclarationForecastAdjuster(var cap: Double) : PrimaryDeclarationForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDeclarationForecast, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryDeclarationForecastAdjuster(var step: Double) : PrimaryDeclarationForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDeclarationForecast, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryDeclarationForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryDeclarationForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryDeclarationForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryDeclarationForecastAdjuster(var minimum: Double) : PrimaryDeclarationForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDeclarationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NamePrimaryDeclarationForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDeclarationForecastStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryDeclarationForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary declaration forecast steps in order and keeps a trace of each. */
data class PrimaryDeclarationForecastEngine(
    var steps: MutableList<PrimaryDeclarationForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryDeclarationForecast): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDeclarationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryDeclarationForecastEngine(flags: Registry): PrimaryDeclarationForecastEngine {
    return PrimaryDeclarationForecastEngine(
        steps = mutableListOf<PrimaryDeclarationForecastStep>(
            FlaggedPrimaryDeclarationForecastAdjuster(flags, "primary-declaration-forecast-lenient", -9.0),
            CapPrimaryDeclarationForecastAdjuster(5731.0),
            RoundingPrimaryDeclarationForecastAdjuster(0.25),
            TieredPrimaryDeclarationForecastAdjuster(
                mutableListOf<Double>(5473.0, 7342.0, 7869.0, 8225.0),
                mutableListOf<Double>(0.84, 1.07, 0.83, 1.17),
            ),
            MinimumPrimaryDeclarationForecastAdjuster(16.9),
            NamePrimaryDeclarationForecastAdjuster(
                mutableMapOf<String, Double>("retail" to 1.08, "central" to 1.39),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 13291.5,
    )
}
