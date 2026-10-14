package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard batch calculation. */
interface ScorecardBatchModifier {
    fun describe(): String
    fun adjust(item: ScorecardBatch, value: Double): Double
}

data class PercentScorecardBatchAdjuster(var percent: Double) : ScorecardBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScorecardBatchExpiresAtOffsetAdjuster(var weight: Double) : ScorecardBatchModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ScorecardBatch, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingScorecardBatchAdjuster(var step: Double) : ScorecardBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the scorecard batch steps in order and keeps a trace of each. */
data class ScorecardBatchEngine(
    var steps: MutableList<ScorecardBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScorecardBatch): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScorecardBatchEngine(): ScorecardBatchEngine {
    return ScorecardBatchEngine(
        steps = mutableListOf<ScorecardBatchModifier>(
            PercentScorecardBatchAdjuster(21.2),
            ScorecardBatchExpiresAtOffsetAdjuster(1.91),
            RoundingScorecardBatchAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 58443.6,
    )
}
