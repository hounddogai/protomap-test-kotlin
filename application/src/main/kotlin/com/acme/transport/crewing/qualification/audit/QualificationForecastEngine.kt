package com.acme.transport.crewing.qualification.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification forecast calculation. */
interface QualificationForecastStep {
    fun describe(): String
    fun adjust(item: QualificationForecast, value: Double): Double
}

data class RoundingQualificationForecastAdjuster(var step: Double) : QualificationForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationForecast, value: Double): Double = roundTo(value, step)
}

data class PercentQualificationForecastAdjuster(var percent: Double) : QualificationForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredQualificationForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumQualificationForecastAdjuster(var minimum: Double) : QualificationForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class QualificationForecastSequenceOffsetAdjuster(var weight: Double) : QualificationForecastStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: QualificationForecast, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the qualification forecast steps in order and keeps a trace of each. */
data class QualificationForecastEngine(
    var steps: MutableList<QualificationForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationForecast): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationForecastEngine(): QualificationForecastEngine {
    return QualificationForecastEngine(
        steps = mutableListOf<QualificationForecastStep>(
            RoundingQualificationForecastAdjuster(5.0),
            PercentQualificationForecastAdjuster(12.0),
            TieredQualificationForecastAdjuster(
                mutableListOf<Double>(1313.0, 5359.0, 5681.0),
                mutableListOf<Double>(0.99, 0.89, 1.18),
            ),
            MinimumQualificationForecastAdjuster(31.4),
            QualificationForecastSequenceOffsetAdjuster(1.07),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 24475.2,
    )
}
