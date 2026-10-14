package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive tariff metric calculation. */
interface AdaptiveTariffMetricModifier {
    fun describe(): String
    fun adjust(item: AdaptiveTariffMetric, value: Double): Double
}

data class CapAdaptiveTariffMetricAdjuster(var cap: Double) : AdaptiveTariffMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveTariffMetric, value: Double): Double = minOf(value, cap)
}

data class AdaptiveTariffMetricStopCountOffsetAdjuster(var weight: Double) : AdaptiveTariffMetricModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: AdaptiveTariffMetric, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class MinimumAdaptiveTariffMetricAdjuster(var minimum: Double) : AdaptiveTariffMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveTariffMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveTariffMetricAdjuster(var step: Double) : AdaptiveTariffMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveTariffMetric, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveTariffMetricAdjuster(var percent: Double) : AdaptiveTariffMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveTariffMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive tariff metric steps in order and keeps a trace of each. */
data class AdaptiveTariffMetricEngine(
    var steps: MutableList<AdaptiveTariffMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveTariffMetric): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveTariffMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveTariffMetricEngine(): AdaptiveTariffMetricEngine {
    return AdaptiveTariffMetricEngine(
        steps = mutableListOf<AdaptiveTariffMetricModifier>(
            CapAdaptiveTariffMetricAdjuster(30437.0),
            AdaptiveTariffMetricStopCountOffsetAdjuster(0.99),
            MinimumAdaptiveTariffMetricAdjuster(17.8),
            RoundingAdaptiveTariffMetricAdjuster(1.0),
            PercentAdaptiveTariffMetricAdjuster(-7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 97383.8,
    )
}
