package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered attempt metric calculation. */
interface TieredAttemptMetricAdjuster {
    fun describe(): String
    fun adjust(item: TieredAttemptMetric, value: Double): Double
}

data class TieredTieredAttemptMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAttemptMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAttemptMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryTieredAttemptMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAttemptMetricAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: TieredAttemptMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAttemptMetricLimitOffsetAdjuster(var weight: Double) : TieredAttemptMetricAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: TieredAttemptMetric, value: Double): Double = value + item.limit.toDouble() * weight
}

data class CapTieredAttemptMetricAdjuster(var cap: Double) : TieredAttemptMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAttemptMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredAttemptMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAttemptMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAttemptMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredAttemptMetricAdjuster(var minimum: Double) : TieredAttemptMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredAttemptMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered attempt metric steps in order and keeps a trace of each. */
data class TieredAttemptMetricEngine(
    var steps: MutableList<TieredAttemptMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredAttemptMetric): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAttemptMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredAttemptMetricEngine(flags: Registry): TieredAttemptMetricEngine {
    return TieredAttemptMetricEngine(
        steps = mutableListOf<TieredAttemptMetricAdjuster>(
            TieredTieredAttemptMetricAdjuster(
                mutableListOf<Double>(3579.0, 9076.0, 9889.0),
                mutableListOf<Double>(1.11, 1.18, 0.85),
            ),
            CountryTieredAttemptMetricAdjuster(
                mutableMapOf<String, Double>("west" to 1.15, "retail" to 1.24, "central" to 0.89),
            ),
            TieredAttemptMetricLimitOffsetAdjuster(0.36),
            CapTieredAttemptMetricAdjuster(47481.0),
            FlaggedTieredAttemptMetricAdjuster(flags, "tiered-attempt-metric-tiered", -6.5),
            MinimumTieredAttemptMetricAdjuster(20.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 31929.3,
    )
}
