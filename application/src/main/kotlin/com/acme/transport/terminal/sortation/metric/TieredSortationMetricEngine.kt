package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered sortation metric calculation. */
interface TieredSortationMetricModifier {
    fun describe(): String
    fun adjust(item: TieredSortationMetric, value: Double): Double
}

data class PercentTieredSortationMetricAdjuster(var percent: Double) : TieredSortationMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredSortationMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredSortationMetricAdjuster(var minimum: Double) : TieredSortationMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredSortationMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSortationMetricRatioOffsetAdjuster(var weight: Double) : TieredSortationMetricModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: TieredSortationMetric, value: Double): Double = value + item.ratio * weight
}

data class TieredTieredSortationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredSortationMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredSortationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered sortation metric steps in order and keeps a trace of each. */
data class TieredSortationMetricEngine(
    var steps: MutableList<TieredSortationMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredSortationMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredSortationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredSortationMetricEngine(): TieredSortationMetricEngine {
    return TieredSortationMetricEngine(
        steps = mutableListOf<TieredSortationMetricModifier>(
            PercentTieredSortationMetricAdjuster(21.2),
            MinimumTieredSortationMetricAdjuster(1.9),
            TieredSortationMetricRatioOffsetAdjuster(1.72),
            TieredTieredSortationMetricAdjuster(
                mutableListOf<Double>(5805.0, 8309.0),
                mutableListOf<Double>(1.09, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 57866.2,
    )
}
