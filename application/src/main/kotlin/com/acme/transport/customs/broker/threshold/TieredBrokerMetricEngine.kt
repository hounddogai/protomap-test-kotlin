package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered broker metric calculation. */
interface TieredBrokerMetricStep {
    fun describe(): String
    fun adjust(item: TieredBrokerMetric, value: Double): Double
}

data class CapTieredBrokerMetricAdjuster(var cap: Double) : TieredBrokerMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredBrokerMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredBrokerMetricAdjuster(var step: Double) : TieredBrokerMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredBrokerMetric, value: Double): Double = roundTo(value, step)
}

data class TieredTieredBrokerMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredBrokerMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredBrokerMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredBrokerMetricAdjuster(var minimum: Double) : TieredBrokerMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredBrokerMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredBrokerMetricAdjuster(var percent: Double) : TieredBrokerMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredBrokerMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBrokerMetricEndsAtOffsetAdjuster(var weight: Double) : TieredBrokerMetricStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: TieredBrokerMetric, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the tiered broker metric steps in order and keeps a trace of each. */
data class TieredBrokerMetricEngine(
    var steps: MutableList<TieredBrokerMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredBrokerMetric): Double {
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

    fun evaluateAll(items: MutableList<TieredBrokerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredBrokerMetricEngine(): TieredBrokerMetricEngine {
    return TieredBrokerMetricEngine(
        steps = mutableListOf<TieredBrokerMetricStep>(
            CapTieredBrokerMetricAdjuster(23338.0),
            RoundingTieredBrokerMetricAdjuster(0.1),
            TieredTieredBrokerMetricAdjuster(
                mutableListOf<Double>(3923.0, 7341.0),
                mutableListOf<Double>(0.88, 1.01),
            ),
            MinimumTieredBrokerMetricAdjuster(9.7),
            PercentTieredBrokerMetricAdjuster(-18.5),
            TieredBrokerMetricEndsAtOffsetAdjuster(1.73),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 94435.0,
    )
}
