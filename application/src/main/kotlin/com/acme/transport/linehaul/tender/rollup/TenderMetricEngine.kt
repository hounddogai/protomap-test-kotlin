package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender metric calculation. */
interface TenderMetricAdjuster {
    fun describe(): String
    fun adjust(item: TenderMetric, value: Double): Double
}

data class MinimumTenderMetricAdjuster(var minimum: Double) : TenderMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotTenderMetricAdjuster(var factors: MutableMap<String, Double>) : TenderMetricAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: TenderMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTenderMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTenderMetricAdjuster(var step: Double) : TenderMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderMetric, value: Double): Double = roundTo(value, step)
}

data class CapTenderMetricAdjuster(var cap: Double) : TenderMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedTenderMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tender metric steps in order and keeps a trace of each. */
data class TenderMetricEngine(
    var steps: MutableList<TenderMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TenderMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderMetricEngine(flags: Registry): TenderMetricEngine {
    return TenderMetricEngine(
        steps = mutableListOf<TenderMetricAdjuster>(
            MinimumTenderMetricAdjuster(5.7),
            DepotTenderMetricAdjuster(mutableMapOf<String, Double>("west" to 0.88)),
            TieredTenderMetricAdjuster(
                mutableListOf<Double>(5105.0, 8604.0, 9303.0, 9796.0),
                mutableListOf<Double>(1.19, 0.88, 0.81, 1.03),
            ),
            RoundingTenderMetricAdjuster(5.0),
            CapTenderMetricAdjuster(47508.0),
            FlaggedTenderMetricAdjuster(flags, "tender-metric-rolling", -2.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 20619.7,
    )
}
