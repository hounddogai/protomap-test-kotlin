package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched duty period forecast calculation. */
interface BatchedDutyPeriodForecastAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDutyPeriodForecast, value: Double): Double
}

data class CapBatchedDutyPeriodForecastAdjuster(var cap: Double) : BatchedDutyPeriodForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDutyPeriodForecast, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedDutyPeriodForecastAdjuster(var percent: Double) : BatchedDutyPeriodForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDutyPeriodForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedDutyPeriodForecastAdjuster(var step: Double) : BatchedDutyPeriodForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDutyPeriodForecast, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedDutyPeriodForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDutyPeriodForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDutyPeriodForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched duty period forecast steps in order and keeps a trace of each. */
data class BatchedDutyPeriodForecastEngine(
    var steps: MutableList<BatchedDutyPeriodForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDutyPeriodForecast): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDutyPeriodForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDutyPeriodForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDutyPeriodForecastEngine(): BatchedDutyPeriodForecastEngine {
    return BatchedDutyPeriodForecastEngine(
        steps = mutableListOf<BatchedDutyPeriodForecastAdjuster>(
            CapBatchedDutyPeriodForecastAdjuster(30074.0),
            PercentBatchedDutyPeriodForecastAdjuster(8.2),
            RoundingBatchedDutyPeriodForecastAdjuster(5.0),
            TieredBatchedDutyPeriodForecastAdjuster(
                mutableListOf<Double>(1538.0, 5206.0, 6385.0, 8039.0),
                mutableListOf<Double>(0.97, 0.92, 0.86, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 77214.4,
    )
}
