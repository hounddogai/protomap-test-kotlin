package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered trailer yard metric calculation. */
interface TieredTrailerYardMetricAdjuster {
    fun describe(): String
    fun adjust(item: TieredTrailerYardMetric, value: Double): Double
}

data class LabelTieredTrailerYardMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTrailerYardMetricAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredTrailerYardMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerYardMetricRetriesOffsetAdjuster(var weight: Double) : TieredTrailerYardMetricAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: TieredTrailerYardMetric, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class PercentTieredTrailerYardMetricAdjuster(var percent: Double) : TieredTrailerYardMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTrailerYardMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered trailer yard metric steps in order and keeps a trace of each. */
data class TieredTrailerYardMetricEngine(
    var steps: MutableList<TieredTrailerYardMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredTrailerYardMetric): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredTrailerYardMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredTrailerYardMetricEngine(): TieredTrailerYardMetricEngine {
    return TieredTrailerYardMetricEngine(
        steps = mutableListOf<TieredTrailerYardMetricAdjuster>(
            LabelTieredTrailerYardMetricAdjuster(mutableMapOf<String, Double>("north" to 1.35, "west" to 1.35)),
            TieredTrailerYardMetricRetriesOffsetAdjuster(0.4),
            PercentTieredTrailerYardMetricAdjuster(9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 40976.7,
    )
}
