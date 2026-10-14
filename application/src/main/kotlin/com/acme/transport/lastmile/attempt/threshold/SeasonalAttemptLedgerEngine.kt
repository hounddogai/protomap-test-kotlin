package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal attempt ledger calculation. */
interface SeasonalAttemptLedgerStep {
    fun describe(): String
    fun adjust(item: SeasonalAttemptLedger, value: Double): Double
}

data class CapSeasonalAttemptLedgerAdjuster(var cap: Double) : SeasonalAttemptLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalAttemptLedger, value: Double): Double = minOf(value, cap)
}

data class SeasonalAttemptLedgerAttemptsOffsetAdjuster(var weight: Double) : SeasonalAttemptLedgerStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: SeasonalAttemptLedger, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class RoundingSeasonalAttemptLedgerAdjuster(var step: Double) : SeasonalAttemptLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAttemptLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal attempt ledger steps in order and keeps a trace of each. */
data class SeasonalAttemptLedgerEngine(
    var steps: MutableList<SeasonalAttemptLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalAttemptLedger): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAttemptLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAttemptLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalAttemptLedgerEngine(): SeasonalAttemptLedgerEngine {
    return SeasonalAttemptLedgerEngine(
        steps = mutableListOf<SeasonalAttemptLedgerStep>(
            CapSeasonalAttemptLedgerAdjuster(1971.0),
            SeasonalAttemptLedgerAttemptsOffsetAdjuster(0.2),
            RoundingSeasonalAttemptLedgerAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 16626.4,
    )
}
