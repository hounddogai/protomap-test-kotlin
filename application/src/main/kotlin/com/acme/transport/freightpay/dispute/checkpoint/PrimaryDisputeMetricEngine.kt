package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the primary dispute metric calculation. */
interface PrimaryDisputeMetricAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDisputeMetric, value: Double): Double
}

data class FlaggedPrimaryDisputeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDisputeMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDisputeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryDisputeMetricAdjuster(var percent: Double) : PrimaryDisputeMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDisputeMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourcePrimaryDisputeMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDisputeMetricAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: PrimaryDisputeMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary dispute metric steps in order and keeps a trace of each. */
data class PrimaryDisputeMetricEngine(
    var steps: MutableList<PrimaryDisputeMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryDisputeMetric): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDisputeMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDisputeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryDisputeMetricEngine(flags: Registry): PrimaryDisputeMetricEngine {
    return PrimaryDisputeMetricEngine(
        steps = mutableListOf<PrimaryDisputeMetricAdjuster>(
            FlaggedPrimaryDisputeMetricAdjuster(flags, "primary-dispute-metric-regional", -3.9),
            PercentPrimaryDisputeMetricAdjuster(-17.0),
            SourcePrimaryDisputeMetricAdjuster(
                mutableMapOf<String, Double>("south" to 0.92, "west" to 1.26, "east" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 20461.1,
    )
}
