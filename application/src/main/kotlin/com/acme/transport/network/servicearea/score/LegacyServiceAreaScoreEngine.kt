package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy service area score calculation. */
interface LegacyServiceAreaScoreStep {
    fun describe(): String
    fun adjust(item: LegacyServiceAreaScore, value: Double): Double
}

data class RoundingLegacyServiceAreaScoreAdjuster(var step: Double) : LegacyServiceAreaScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyServiceAreaScore, value: Double): Double = roundTo(value, step)
}

data class LegacyServiceAreaScoreEtaMinutesOffsetAdjuster(var weight: Double) : LegacyServiceAreaScoreStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: LegacyServiceAreaScore, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapLegacyServiceAreaScoreAdjuster(var cap: Double) : LegacyServiceAreaScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyServiceAreaScore, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyServiceAreaScoreAdjuster(var percent: Double) : LegacyServiceAreaScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyServiceAreaScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyServiceAreaScoreAdjuster(var minimum: Double) : LegacyServiceAreaScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyServiceAreaScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy service area score steps in order and keeps a trace of each. */
data class LegacyServiceAreaScoreEngine(
    var steps: MutableList<LegacyServiceAreaScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyServiceAreaScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyServiceAreaScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyServiceAreaScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyServiceAreaScoreEngine(): LegacyServiceAreaScoreEngine {
    return LegacyServiceAreaScoreEngine(
        steps = mutableListOf<LegacyServiceAreaScoreStep>(
            RoundingLegacyServiceAreaScoreAdjuster(0.1),
            LegacyServiceAreaScoreEtaMinutesOffsetAdjuster(1.74),
            CapLegacyServiceAreaScoreAdjuster(34646.0),
            PercentLegacyServiceAreaScoreAdjuster(-8.8),
            MinimumLegacyServiceAreaScoreAdjuster(11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 53300.1,
    )
}
