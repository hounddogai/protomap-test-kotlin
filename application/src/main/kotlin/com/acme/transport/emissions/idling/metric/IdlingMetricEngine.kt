package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling metric calculation. */
interface IdlingMetricStep {
    fun describe(): String
    fun adjust(item: IdlingMetric, value: Double): Double
}

data class IdlingMetricScoreOffsetAdjuster(var weight: Double) : IdlingMetricStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: IdlingMetric, value: Double): Double = value + item.score * weight
}

data class MinimumIdlingMetricAdjuster(var minimum: Double) : IdlingMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredIdlingMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryIdlingMetricAdjuster(var factors: MutableMap<String, Double>) : IdlingMetricStep {
    override fun describe(): String = "by category"

    override fun adjust(item: IdlingMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingIdlingMetricAdjuster(var step: Double) : IdlingMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the idling metric steps in order and keeps a trace of each. */
data class IdlingMetricEngine(
    var steps: MutableList<IdlingMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingMetric): Double {
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

    fun evaluateAll(items: MutableList<IdlingMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingMetricEngine(): IdlingMetricEngine {
    return IdlingMetricEngine(
        steps = mutableListOf<IdlingMetricStep>(
            IdlingMetricScoreOffsetAdjuster(0.03),
            MinimumIdlingMetricAdjuster(38.7),
            TieredIdlingMetricAdjuster(
                mutableListOf<Double>(250.0, 653.0, 2591.0, 9741.0),
                mutableListOf<Double>(0.86, 1.1, 0.82, 1.2),
            ),
            CategoryIdlingMetricAdjuster(
                mutableMapOf<String, Double>("west" to 1.29, "north" to 0.92, "east" to 0.83),
            ),
            RoundingIdlingMetricAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 56868.2,
    )
}
