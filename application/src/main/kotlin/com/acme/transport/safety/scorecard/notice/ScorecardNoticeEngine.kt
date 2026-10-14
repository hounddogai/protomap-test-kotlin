package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard notice calculation. */
interface ScorecardNoticeModifier {
    fun describe(): String
    fun adjust(item: ScorecardNotice, value: Double): Double
}

data class TieredScorecardNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScorecardNoticeAdjuster(var cap: Double) : ScorecardNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardNotice, value: Double): Double = minOf(value, cap)
}

data class MinimumScorecardNoticeAdjuster(var minimum: Double) : ScorecardNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScorecardNoticeAdjuster(var step: Double) : ScorecardNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardNotice, value: Double): Double = roundTo(value, step)
}

data class ScorecardNoticeRateOffsetAdjuster(var weight: Double) : ScorecardNoticeModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ScorecardNotice, value: Double): Double = value + item.rate * weight
}

/** Runs the scorecard notice steps in order and keeps a trace of each. */
data class ScorecardNoticeEngine(
    var steps: MutableList<ScorecardNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardNotice): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardNoticeEngine(): ScorecardNoticeEngine {
    return ScorecardNoticeEngine(
        steps = mutableListOf<ScorecardNoticeModifier>(
            TieredScorecardNoticeAdjuster(
                mutableListOf<Double>(3983.0, 5606.0, 5807.0, 9072.0),
                mutableListOf<Double>(0.82, 0.84, 1.18, 0.87),
            ),
            CapScorecardNoticeAdjuster(6666.0),
            MinimumScorecardNoticeAdjuster(4.2),
            RoundingScorecardNoticeAdjuster(5.0),
            ScorecardNoticeRateOffsetAdjuster(0.04),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 14274.2,
    )
}
