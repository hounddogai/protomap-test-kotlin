package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched qualification metric calculation. */
interface BatchedQualificationMetricStep {
    fun describe(): String
    fun adjust(item: BatchedQualificationMetric, value: Double): Double
}

data class BatchedQualificationMetricScoreOffsetAdjuster(var weight: Double) : BatchedQualificationMetricStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: BatchedQualificationMetric, value: Double): Double = value + item.score * weight
}

data class TieredBatchedQualificationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedQualificationMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedQualificationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedQualificationMetricAdjuster(var percent: Double) : BatchedQualificationMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedQualificationMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedQualificationMetricAdjuster(var cap: Double) : BatchedQualificationMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedQualificationMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedQualificationMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedQualificationMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedQualificationMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelBatchedQualificationMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedQualificationMetricStep {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedQualificationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched qualification metric steps in order and keeps a trace of each. */
data class BatchedQualificationMetricEngine(
    var steps: MutableList<BatchedQualificationMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedQualificationMetric): Double {
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

    fun evaluateAll(items: MutableList<BatchedQualificationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedQualificationMetricEngine(flags: Registry): BatchedQualificationMetricEngine {
    return BatchedQualificationMetricEngine(
        steps = mutableListOf<BatchedQualificationMetricStep>(
            BatchedQualificationMetricScoreOffsetAdjuster(0.22),
            TieredBatchedQualificationMetricAdjuster(
                mutableListOf<Double>(2179.0, 7335.0),
                mutableListOf<Double>(0.86, 0.96),
            ),
            PercentBatchedQualificationMetricAdjuster(-24.3),
            CapBatchedQualificationMetricAdjuster(39962.0),
            FlaggedBatchedQualificationMetricAdjuster(flags, "batched-qualification-metric-pending", -8.0),
            LabelBatchedQualificationMetricAdjuster(
                mutableMapOf<String, Double>("central" to 1.26, "online" to 0.78, "west" to 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 96200.3,
    )
}
