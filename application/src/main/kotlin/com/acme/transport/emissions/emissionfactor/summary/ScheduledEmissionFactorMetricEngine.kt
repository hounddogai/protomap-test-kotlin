package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled emission factor metric calculation. */
interface ScheduledEmissionFactorMetricFactor {
    fun describe(): String
    fun adjust(item: ScheduledEmissionFactorMetric, value: Double): Double
}

data class CapScheduledEmissionFactorMetricAdjuster(var cap: Double) : ScheduledEmissionFactorMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledEmissionFactorMetric, value: Double): Double = minOf(value, cap)
}

data class UnitScheduledEmissionFactorMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledEmissionFactorMetricFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledEmissionFactorMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScheduledEmissionFactorMetricAdjuster(
    var percent: Double,
) : ScheduledEmissionFactorMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledEmissionFactorMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledEmissionFactorMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledEmissionFactorMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledEmissionFactorMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScheduledEmissionFactorMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledEmissionFactorMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledEmissionFactorMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled emission factor metric steps in order and keeps a trace of each. */
data class ScheduledEmissionFactorMetricEngine(
    var steps: MutableList<ScheduledEmissionFactorMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledEmissionFactorMetric): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledEmissionFactorMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledEmissionFactorMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledEmissionFactorMetricEngine(flags: Registry): ScheduledEmissionFactorMetricEngine {
    return ScheduledEmissionFactorMetricEngine(
        steps = mutableListOf<ScheduledEmissionFactorMetricFactor>(
            CapScheduledEmissionFactorMetricAdjuster(39450.0),
            UnitScheduledEmissionFactorMetricAdjuster(
                mutableMapOf<String, Double>("north" to 1.23, "retail" to 1.22),
            ),
            PercentScheduledEmissionFactorMetricAdjuster(2.3),
            FlaggedScheduledEmissionFactorMetricAdjuster(flags, "scheduled-emission-factor-metric-tiered", -2.3),
            TieredScheduledEmissionFactorMetricAdjuster(
                mutableListOf<Double>(4940.0, 6299.0),
                mutableListOf<Double>(0.84, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 17296.3,
    )
}
