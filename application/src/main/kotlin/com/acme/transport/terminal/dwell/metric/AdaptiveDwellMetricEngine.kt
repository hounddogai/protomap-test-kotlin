package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive dwell metric calculation. */
interface AdaptiveDwellMetricModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDwellMetric, value: Double): Double
}

data class TieredAdaptiveDwellMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDwellMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDwellMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveDwellMetricAdjuster(var minimum: Double) : AdaptiveDwellMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDwellMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveDwellMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDwellMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDwellMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveDwellMetricWindowDaysOffsetAdjuster(var weight: Double) : AdaptiveDwellMetricModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AdaptiveDwellMetric, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the adaptive dwell metric steps in order and keeps a trace of each. */
data class AdaptiveDwellMetricEngine(
    var steps: MutableList<AdaptiveDwellMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDwellMetric): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDwellMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDwellMetricEngine(flags: Registry): AdaptiveDwellMetricEngine {
    return AdaptiveDwellMetricEngine(
        steps = mutableListOf<AdaptiveDwellMetricModifier>(
            TieredAdaptiveDwellMetricAdjuster(
                mutableListOf<Double>(3168.0, 4351.0),
                mutableListOf<Double>(1.12, 1.14),
            ),
            MinimumAdaptiveDwellMetricAdjuster(45.0),
            FlaggedAdaptiveDwellMetricAdjuster(flags, "adaptive-dwell-metric-scheduled", -3.2),
            AdaptiveDwellMetricWindowDaysOffsetAdjuster(1.51),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 14870.6,
    )
}
