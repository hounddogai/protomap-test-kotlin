package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default attempt claim calculation. */
interface DefaultAttemptClaimStep {
    fun describe(): String
    fun adjust(item: DefaultAttemptClaim, value: Double): Double
}

data class RoundingDefaultAttemptClaimAdjuster(var step: Double) : DefaultAttemptClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAttemptClaim, value: Double): Double = roundTo(value, step)
}

data class PercentDefaultAttemptClaimAdjuster(var percent: Double) : DefaultAttemptClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultAttemptClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefaultAttemptClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAttemptClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAttemptClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultAttemptClaimAdjuster(var cap: Double) : DefaultAttemptClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAttemptClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedDefaultAttemptClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAttemptClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAttemptClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default attempt claim steps in order and keeps a trace of each. */
data class DefaultAttemptClaimEngine(
    var steps: MutableList<DefaultAttemptClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultAttemptClaim): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAttemptClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAttemptClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultAttemptClaimEngine(flags: Registry): DefaultAttemptClaimEngine {
    return DefaultAttemptClaimEngine(
        steps = mutableListOf<DefaultAttemptClaimStep>(
            RoundingDefaultAttemptClaimAdjuster(0.25),
            PercentDefaultAttemptClaimAdjuster(-3.7),
            TieredDefaultAttemptClaimAdjuster(
                mutableListOf<Double>(6388.0, 9195.0),
                mutableListOf<Double>(1.08, 0.81),
            ),
            CapDefaultAttemptClaimAdjuster(1274.0),
            FlaggedDefaultAttemptClaimAdjuster(flags, "default-attempt-claim-weighted", -3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 78738.7,
    )
}
