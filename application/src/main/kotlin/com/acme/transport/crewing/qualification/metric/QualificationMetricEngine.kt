package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.flags.Registry

/** One step of the qualification metric calculation. */
interface QualificationMetricFactor {
    fun describe(): String
    fun adjust(item: QualificationMetric, value: Double): Double
}

data class QualificationMetricWindowDaysOffsetAdjuster(var weight: Double) : QualificationMetricFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: QualificationMetric, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class FlaggedQualificationMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredQualificationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentQualificationMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationMetricFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: QualificationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapQualificationMetricAdjuster(var cap: Double) : QualificationMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the qualification metric steps in order and keeps a trace of each. */
data class QualificationMetricEngine(
    var steps: MutableList<QualificationMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationMetric): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationMetricEngine(flags: Registry): QualificationMetricEngine {
    return QualificationMetricEngine(
        steps = mutableListOf<QualificationMetricFactor>(
            QualificationMetricWindowDaysOffsetAdjuster(0.31),
            FlaggedQualificationMetricAdjuster(flags, "qualification-metric-rolling", -7.1),
            TieredQualificationMetricAdjuster(
                mutableListOf<Double>(1143.0, 1512.0, 3783.0, 7545.0),
                mutableListOf<Double>(0.92, 0.9, 1.09, 0.98),
            ),
            SegmentQualificationMetricAdjuster(mutableMapOf<String, Double>("north" to 0.85)),
            CapQualificationMetricAdjuster(46144.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 11936.0,
    )
}
