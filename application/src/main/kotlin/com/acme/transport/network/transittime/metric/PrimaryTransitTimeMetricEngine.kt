package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary transit time metric calculation. */
interface PrimaryTransitTimeMetricAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double
}

data class CountryPrimaryTransitTimeMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTransitTimeMetricAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryTransitTimeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTransitTimeMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryTransitTimeMetricAdjuster(var percent: Double) : PrimaryTransitTimeMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryTransitTimeMetricStopCountOffsetAdjuster(
    var weight: Double,
) : PrimaryTransitTimeMetricAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class TieredPrimaryTransitTimeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTransitTimeMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryTransitTimeMetricAdjuster(var step: Double) : PrimaryTransitTimeMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTransitTimeMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the primary transit time metric steps in order and keeps a trace of each. */
data class PrimaryTransitTimeMetricEngine(
    var steps: MutableList<PrimaryTransitTimeMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryTransitTimeMetric): Double {
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

    fun evaluateAll(items: MutableList<PrimaryTransitTimeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryTransitTimeMetricEngine(flags: Registry): PrimaryTransitTimeMetricEngine {
    return PrimaryTransitTimeMetricEngine(
        steps = mutableListOf<PrimaryTransitTimeMetricAdjuster>(
            CountryPrimaryTransitTimeMetricAdjuster(
                mutableMapOf<String, Double>("north" to 1.26, "retail" to 1.1, "west" to 0.85),
            ),
            FlaggedPrimaryTransitTimeMetricAdjuster(flags, "primary-transit-time-metric-global", -4.8),
            PercentPrimaryTransitTimeMetricAdjuster(22.5),
            PrimaryTransitTimeMetricStopCountOffsetAdjuster(1.09),
            TieredPrimaryTransitTimeMetricAdjuster(
                mutableListOf<Double>(3900.0, 6613.0),
                mutableListOf<Double>(1.09, 0.89),
            ),
            RoundingPrimaryTransitTimeMetricAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 45600.9,
    )
}
