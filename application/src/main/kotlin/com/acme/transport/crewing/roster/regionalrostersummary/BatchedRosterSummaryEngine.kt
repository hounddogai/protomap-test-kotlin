package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched roster summary calculation. */
interface BatchedRosterSummaryStep {
    fun describe(): String
    fun adjust(item: BatchedRosterSummary, value: Double): Double
}

data class FlaggedBatchedRosterSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRosterSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRosterSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedRosterSummaryAdjuster(var percent: Double) : BatchedRosterSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRosterSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelBatchedRosterSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRosterSummaryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedRosterSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedRosterSummaryAdjuster(var minimum: Double) : BatchedRosterSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRosterSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedRosterSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRosterSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRosterSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched roster summary steps in order and keeps a trace of each. */
data class BatchedRosterSummaryEngine(
    var steps: MutableList<BatchedRosterSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRosterSummary): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRosterSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRosterSummaryEngine(flags: Registry): BatchedRosterSummaryEngine {
    return BatchedRosterSummaryEngine(
        steps = mutableListOf<BatchedRosterSummaryStep>(
            FlaggedBatchedRosterSummaryAdjuster(flags, "batched-roster-summary-manual", 6.0),
            PercentBatchedRosterSummaryAdjuster(5.6),
            LabelBatchedRosterSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.11)),
            MinimumBatchedRosterSummaryAdjuster(4.8),
            TieredBatchedRosterSummaryAdjuster(
                mutableListOf<Double>(2651.0, 4483.0, 6474.0),
                mutableListOf<Double>(1.09, 1.07, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 26261.9,
    )
}
