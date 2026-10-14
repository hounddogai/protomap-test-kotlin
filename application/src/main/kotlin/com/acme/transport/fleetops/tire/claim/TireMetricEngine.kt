package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire metric calculation. */
interface TireMetricFactor {
    fun describe(): String
    fun adjust(item: TireMetric, value: Double): Double
}

data class TieredTireMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTireMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTireMetricAdjuster(var step: Double) : TireMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireMetric, value: Double): Double = roundTo(value, step)
}

data class TireMetricExpiresAtOffsetAdjuster(var weight: Double) : TireMetricFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: TireMetric, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class LocaleTireMetricAdjuster(var factors: MutableMap<String, Double>) : TireMetricFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: TireMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTireMetricAdjuster(var cap: Double) : TireMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the tire metric steps in order and keeps a trace of each. */
data class TireMetricEngine(
    var steps: MutableList<TireMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireMetric): Double {
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

    fun evaluateAll(items: MutableList<TireMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireMetricEngine(flags: Registry): TireMetricEngine {
    return TireMetricEngine(
        steps = mutableListOf<TireMetricFactor>(
            TieredTireMetricAdjuster(mutableListOf<Double>(7551.0, 7931.0), mutableListOf<Double>(0.85, 0.82)),
            FlaggedTireMetricAdjuster(flags, "tire-metric-regional", 2.7),
            RoundingTireMetricAdjuster(1.0),
            TireMetricExpiresAtOffsetAdjuster(0.09),
            LocaleTireMetricAdjuster(mutableMapOf<String, Double>("east" to 0.94)),
            CapTireMetricAdjuster(9806.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 58980.5,
    )
}
