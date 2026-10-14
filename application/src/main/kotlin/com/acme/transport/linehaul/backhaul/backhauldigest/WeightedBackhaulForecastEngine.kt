package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted backhaul forecast calculation. */
interface WeightedBackhaulForecastStep {
    fun describe(): String
    fun adjust(item: WeightedBackhaulForecast, value: Double): Double
}

data class RoundingWeightedBackhaulForecastAdjuster(var step: Double) : WeightedBackhaulForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedBackhaulForecast, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedBackhaulForecastAdjuster(var percent: Double) : WeightedBackhaulForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedBackhaulForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedBackhaulForecastAdjuster(var cap: Double) : WeightedBackhaulForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedBackhaulForecast, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedBackhaulForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedBackhaulForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedBackhaulForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleWeightedBackhaulForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedBackhaulForecastStep {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedBackhaulForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedBackhaulForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedBackhaulForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedBackhaulForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted backhaul forecast steps in order and keeps a trace of each. */
data class WeightedBackhaulForecastEngine(
    var steps: MutableList<WeightedBackhaulForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedBackhaulForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedBackhaulForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedBackhaulForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedBackhaulForecastEngine(flags: Registry): WeightedBackhaulForecastEngine {
    return WeightedBackhaulForecastEngine(
        steps = mutableListOf<WeightedBackhaulForecastStep>(
            RoundingWeightedBackhaulForecastAdjuster(5.0),
            PercentWeightedBackhaulForecastAdjuster(-7.4),
            CapWeightedBackhaulForecastAdjuster(17407.0),
            TieredWeightedBackhaulForecastAdjuster(
                mutableListOf<Double>(312.0, 1297.0, 7380.0),
                mutableListOf<Double>(1.06, 1.01, 1.06),
            ),
            TitleWeightedBackhaulForecastAdjuster(mutableMapOf<String, Double>("west" to 0.89)),
            FlaggedWeightedBackhaulForecastAdjuster(flags, "weighted-backhaul-forecast-strict", 9.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 46506.5,
    )
}
