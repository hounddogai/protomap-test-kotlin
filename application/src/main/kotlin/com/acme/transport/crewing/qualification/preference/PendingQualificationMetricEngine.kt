package com.acme.transport.crewing.qualification.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending qualification metric calculation. */
interface PendingQualificationMetricModifier {
    fun describe(): String
    fun adjust(item: PendingQualificationMetric, value: Double): Double
}

data class FlaggedPendingQualificationMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingQualificationMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingQualificationMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingQualificationMetricAdjuster(var minimum: Double) : PendingQualificationMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingQualificationMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelPendingQualificationMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingQualificationMetricModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingQualificationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPendingQualificationMetricAdjuster(var cap: Double) : PendingQualificationMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingQualificationMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingQualificationMetricAdjuster(var step: Double) : PendingQualificationMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingQualificationMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the pending qualification metric steps in order and keeps a trace of each. */
data class PendingQualificationMetricEngine(
    var steps: MutableList<PendingQualificationMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingQualificationMetric): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingQualificationMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingQualificationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingQualificationMetricEngine(flags: Registry): PendingQualificationMetricEngine {
    return PendingQualificationMetricEngine(
        steps = mutableListOf<PendingQualificationMetricModifier>(
            FlaggedPendingQualificationMetricAdjuster(flags, "pending-qualification-metric-batched", 4.1),
            MinimumPendingQualificationMetricAdjuster(25.9),
            LabelPendingQualificationMetricAdjuster(mutableMapOf<String, Double>("central" to 0.85)),
            CapPendingQualificationMetricAdjuster(41737.0),
            RoundingPendingQualificationMetricAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 65342.9,
    )
}
