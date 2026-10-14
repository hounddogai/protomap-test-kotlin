package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute score calculation. */
interface DisputeScoreStep {
    fun describe(): String
    fun adjust(item: DisputeScore, value: Double): Double
}

data class RoundingDisputeScoreAdjuster(var step: Double) : DisputeScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeScore, value: Double): Double = roundTo(value, step)
}

data class MinimumDisputeScoreAdjuster(var minimum: Double) : DisputeScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDisputeScoreAdjuster(var cap: Double) : DisputeScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeScore, value: Double): Double = minOf(value, cap)
}

data class TieredDisputeScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDisputeScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dispute score steps in order and keeps a trace of each. */
data class DisputeScoreEngine(
    var steps: MutableList<DisputeScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DisputeScore): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDisputeScoreEngine(flags: Registry): DisputeScoreEngine {
    return DisputeScoreEngine(
        steps = mutableListOf<DisputeScoreStep>(
            RoundingDisputeScoreAdjuster(5.0),
            MinimumDisputeScoreAdjuster(39.6),
            CapDisputeScoreAdjuster(36857.0),
            TieredDisputeScoreAdjuster(
                mutableListOf<Double>(2958.0, 3795.0, 8651.0, 9191.0),
                mutableListOf<Double>(1.14, 1.01, 0.86, 1.04),
            ),
            FlaggedDisputeScoreAdjuster(flags, "dispute-score-tiered", 2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 98309.3,
    )
}
