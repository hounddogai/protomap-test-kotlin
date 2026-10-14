package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.flags.Registry

/** One step of the trailer metric calculation. */
interface TrailerMetricStep {
    fun describe(): String
    fun adjust(item: TrailerMetric, value: Double): Double
}

data class FlaggedTrailerMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleTrailerMetricAdjuster(var factors: MutableMap<String, Double>) : TrailerMetricStep {
    override fun describe(): String = "by title"

    override fun adjust(item: TrailerMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerMetricAdjuster(var minimum: Double) : TrailerMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTrailerMetricAdjuster(var percent: Double) : TrailerMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TrailerMetricThresholdOffsetAdjuster(var weight: Double) : TrailerMetricStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: TrailerMetric, value: Double): Double = value + item.threshold * weight
}

/** Runs the trailer metric steps in order and keeps a trace of each. */
data class TrailerMetricEngine(
    var steps: MutableList<TrailerMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerMetric): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerMetricEngine(flags: Registry): TrailerMetricEngine {
    return TrailerMetricEngine(
        steps = mutableListOf<TrailerMetricStep>(
            FlaggedTrailerMetricAdjuster(flags, "trailer-metric-scheduled", -4.8),
            TitleTrailerMetricAdjuster(mutableMapOf<String, Double>("online" to 1.22, "retail" to 0.87)),
            MinimumTrailerMetricAdjuster(33.4),
            TieredTrailerMetricAdjuster(
                mutableListOf<Double>(1363.0, 5913.0, 6983.0),
                mutableListOf<Double>(1.12, 0.92, 1.06),
            ),
            PercentTrailerMetricAdjuster(1.3),
            TrailerMetricThresholdOffsetAdjuster(0.06),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 49410.6,
    )
}
