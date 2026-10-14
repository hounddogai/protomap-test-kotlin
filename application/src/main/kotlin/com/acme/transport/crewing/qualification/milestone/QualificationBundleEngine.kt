package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the qualification bundle calculation. */
interface QualificationBundleStep {
    fun describe(): String
    fun adjust(item: QualificationBundle, value: Double): Double
}

data class FlaggedQualificationBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingQualificationBundleAdjuster(var step: Double) : QualificationBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationBundle, value: Double): Double = roundTo(value, step)
}

data class TieredQualificationBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumQualificationBundleAdjuster(var minimum: Double) : QualificationBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentQualificationBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationBundleStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: QualificationBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class QualificationBundleLoadKgOffsetAdjuster(var weight: Double) : QualificationBundleStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: QualificationBundle, value: Double): Double = value + item.loadKg * weight
}

/** Runs the qualification bundle steps in order and keeps a trace of each. */
data class QualificationBundleEngine(
    var steps: MutableList<QualificationBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationBundle): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationBundleEngine(flags: Registry): QualificationBundleEngine {
    return QualificationBundleEngine(
        steps = mutableListOf<QualificationBundleStep>(
            FlaggedQualificationBundleAdjuster(flags, "qualification-bundle-primary", -5.8),
            RoundingQualificationBundleAdjuster(0.1),
            TieredQualificationBundleAdjuster(
                mutableListOf<Double>(9269.0, 9281.0),
                mutableListOf<Double>(1.16, 1.11),
            ),
            MinimumQualificationBundleAdjuster(38.4),
            SegmentQualificationBundleAdjuster(mutableMapOf<String, Double>("north" to 1.14, "west" to 1.07)),
            QualificationBundleLoadKgOffsetAdjuster(1.03),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 95570.9,
    )
}
