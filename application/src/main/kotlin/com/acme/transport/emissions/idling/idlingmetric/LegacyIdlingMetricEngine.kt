package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy idling metric calculation. */
interface LegacyIdlingMetricAdjuster {
    fun describe(): String
    fun adjust(item: LegacyIdlingMetric, value: Double): Double
}

data class FlaggedLegacyIdlingMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyIdlingMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyIdlingMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyIdlingMetricAdjuster(var cap: Double) : LegacyIdlingMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyIdlingMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyIdlingMetricAdjuster(var step: Double) : LegacyIdlingMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIdlingMetric, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyIdlingMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIdlingMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIdlingMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelLegacyIdlingMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyIdlingMetricAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyIdlingMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyIdlingMetricAdjuster(var minimum: Double) : LegacyIdlingMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyIdlingMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy idling metric steps in order and keeps a trace of each. */
data class LegacyIdlingMetricEngine(
    var steps: MutableList<LegacyIdlingMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyIdlingMetric): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyIdlingMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyIdlingMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyIdlingMetricEngine(flags: Registry): LegacyIdlingMetricEngine {
    return LegacyIdlingMetricEngine(
        steps = mutableListOf<LegacyIdlingMetricAdjuster>(
            FlaggedLegacyIdlingMetricAdjuster(flags, "legacy-idling-metric-capped", -5.6),
            CapLegacyIdlingMetricAdjuster(32322.0),
            RoundingLegacyIdlingMetricAdjuster(0.1),
            TieredLegacyIdlingMetricAdjuster(
                mutableListOf<Double>(663.0, 2166.0, 3429.0, 7100.0),
                mutableListOf<Double>(1.14, 1.13, 1.02, 0.83),
            ),
            LabelLegacyIdlingMetricAdjuster(mutableMapOf<String, Double>("east" to 0.99)),
            MinimumLegacyIdlingMetricAdjuster(45.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 81419.8,
    )
}
