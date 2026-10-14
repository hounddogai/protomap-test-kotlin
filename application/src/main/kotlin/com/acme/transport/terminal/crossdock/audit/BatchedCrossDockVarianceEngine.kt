package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.flags.Registry

/** One step of the batched cross dock variance calculation. */
interface BatchedCrossDockVarianceModifier {
    fun describe(): String
    fun adjust(item: BatchedCrossDockVariance, value: Double): Double
}

data class BatchedCrossDockVarianceWindowDaysOffsetAdjuster(
    var weight: Double,
) : BatchedCrossDockVarianceModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: BatchedCrossDockVariance, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class CategoryBatchedCrossDockVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCrossDockVarianceModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: BatchedCrossDockVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedCrossDockVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCrossDockVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCrossDockVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedCrossDockVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCrossDockVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCrossDockVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedCrossDockVarianceAdjuster(var percent: Double) : BatchedCrossDockVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCrossDockVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched cross dock variance steps in order and keeps a trace of each. */
data class BatchedCrossDockVarianceEngine(
    var steps: MutableList<BatchedCrossDockVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedCrossDockVariance): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedCrossDockVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedCrossDockVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedCrossDockVarianceEngine(flags: Registry): BatchedCrossDockVarianceEngine {
    return BatchedCrossDockVarianceEngine(
        steps = mutableListOf<BatchedCrossDockVarianceModifier>(
            BatchedCrossDockVarianceWindowDaysOffsetAdjuster(0.42),
            CategoryBatchedCrossDockVarianceAdjuster(
                mutableMapOf<String, Double>("south" to 0.88, "central" to 0.71, "east" to 0.89),
            ),
            TieredBatchedCrossDockVarianceAdjuster(
                mutableListOf<Double>(2832.0, 4002.0, 4430.0),
                mutableListOf<Double>(0.97, 0.84, 0.87),
            ),
            FlaggedBatchedCrossDockVarianceAdjuster(flags, "batched-cross-dock-variance-adaptive", 0.1),
            PercentBatchedCrossDockVarianceAdjuster(-0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 23401.4,
    )
}
