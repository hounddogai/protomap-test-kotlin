package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration forecast calculation. */
interface DeclarationForecastAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationForecast, value: Double): Double
}

data class PercentDeclarationForecastAdjuster(var percent: Double) : DeclarationForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DeclarationForecastMarginOffsetAdjuster(var weight: Double) : DeclarationForecastAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DeclarationForecast, value: Double): Double = value + item.margin * weight
}

data class FlaggedDeclarationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the declaration forecast steps in order and keeps a trace of each. */
data class DeclarationForecastEngine(
    var steps: MutableList<DeclarationForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationForecast): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationForecastEngine(flags: Registry): DeclarationForecastEngine {
    return DeclarationForecastEngine(
        steps = mutableListOf<DeclarationForecastAdjuster>(
            PercentDeclarationForecastAdjuster(-17.3),
            DeclarationForecastMarginOffsetAdjuster(0.87),
            FlaggedDeclarationForecastAdjuster(flags, "declaration-forecast-capped", -6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 76843.0,
    )
}
