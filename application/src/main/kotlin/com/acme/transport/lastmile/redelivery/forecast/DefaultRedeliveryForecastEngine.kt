package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default redelivery forecast calculation. */
interface DefaultRedeliveryForecastStep {
    fun describe(): String
    fun adjust(item: DefaultRedeliveryForecast, value: Double): Double
}

data class PercentDefaultRedeliveryForecastAdjuster(var percent: Double) : DefaultRedeliveryForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRedeliveryForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultRedeliveryForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRedeliveryForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRedeliveryForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneDefaultRedeliveryForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRedeliveryForecastStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: DefaultRedeliveryForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultRedeliveryForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRedeliveryForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRedeliveryForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultRedeliveryForecastAdjuster(var cap: Double) : DefaultRedeliveryForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRedeliveryForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the default redelivery forecast steps in order and keeps a trace of each. */
data class DefaultRedeliveryForecastEngine(
    var steps: MutableList<DefaultRedeliveryForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultRedeliveryForecast): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRedeliveryForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultRedeliveryForecastEngine(flags: Registry): DefaultRedeliveryForecastEngine {
    return DefaultRedeliveryForecastEngine(
        steps = mutableListOf<DefaultRedeliveryForecastStep>(
            PercentDefaultRedeliveryForecastAdjuster(-14.6),
            FlaggedDefaultRedeliveryForecastAdjuster(flags, "default-redelivery-forecast-strict", 9.9),
            LaneDefaultRedeliveryForecastAdjuster(mutableMapOf<String, Double>("online" to 0.75)),
            TieredDefaultRedeliveryForecastAdjuster(
                mutableListOf<Double>(2219.0, 5357.0, 9998.0),
                mutableListOf<Double>(0.82, 1.05, 0.82),
            ),
            CapDefaultRedeliveryForecastAdjuster(42362.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 31831.3,
    )
}
