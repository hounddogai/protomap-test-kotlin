package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched scorecard bundle calculation. */
interface BatchedScorecardBundleModifier {
    fun describe(): String
    fun adjust(item: BatchedScorecardBundle, value: Double): Double
}

data class PercentBatchedScorecardBundleAdjuster(var percent: Double) : BatchedScorecardBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedScorecardBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedScorecardBundleWindowDaysOffsetAdjuster(var weight: Double) : BatchedScorecardBundleModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: BatchedScorecardBundle, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class RoundingBatchedScorecardBundleAdjuster(var step: Double) : BatchedScorecardBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedScorecardBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the batched scorecard bundle steps in order and keeps a trace of each. */
data class BatchedScorecardBundleEngine(
    var steps: MutableList<BatchedScorecardBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedScorecardBundle): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedScorecardBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedScorecardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedScorecardBundleEngine(): BatchedScorecardBundleEngine {
    return BatchedScorecardBundleEngine(
        steps = mutableListOf<BatchedScorecardBundleModifier>(
            PercentBatchedScorecardBundleAdjuster(-12.0),
            BatchedScorecardBundleWindowDaysOffsetAdjuster(0.05),
            RoundingBatchedScorecardBundleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 74343.8,
    )
}
