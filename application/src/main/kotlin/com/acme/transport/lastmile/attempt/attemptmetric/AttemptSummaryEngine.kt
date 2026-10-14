package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt summary calculation. */
interface AttemptSummaryStep {
    fun describe(): String
    fun adjust(item: AttemptSummary, value: Double): Double
}

data class RoundingAttemptSummaryAdjuster(var step: Double) : AttemptSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptSummary, value: Double): Double = roundTo(value, step)
}

data class AttemptSummaryEndsAtOffsetAdjuster(var weight: Double) : AttemptSummaryStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: AttemptSummary, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class TieredAttemptSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the attempt summary steps in order and keeps a trace of each. */
data class AttemptSummaryEngine(
    var steps: MutableList<AttemptSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptSummary): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptSummaryEngine(): AttemptSummaryEngine {
    return AttemptSummaryEngine(
        steps = mutableListOf<AttemptSummaryStep>(
            RoundingAttemptSummaryAdjuster(0.25),
            AttemptSummaryEndsAtOffsetAdjuster(1.9),
            TieredAttemptSummaryAdjuster(
                mutableListOf<Double>(1312.0, 2493.0, 2594.0, 3665.0),
                mutableListOf<Double>(0.85, 0.98, 0.83, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 50129.6,
    )
}
