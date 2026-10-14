package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling scorecard policy calculation. */
interface RollingScorecardPolicyAdjuster {
    fun describe(): String
    fun adjust(item: RollingScorecardPolicy, value: Double): Double
}

data class RoundingRollingScorecardPolicyAdjuster(var step: Double) : RollingScorecardPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingScorecardPolicy, value: Double): Double = roundTo(value, step)
}

data class CapRollingScorecardPolicyAdjuster(var cap: Double) : RollingScorecardPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingScorecardPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingScorecardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingScorecardPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingScorecardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryRollingScorecardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingScorecardPolicyAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: RollingScorecardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingScorecardPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingScorecardPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingScorecardPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling scorecard policy steps in order and keeps a trace of each. */
data class RollingScorecardPolicyEngine(
    var steps: MutableList<RollingScorecardPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingScorecardPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingScorecardPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingScorecardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingScorecardPolicyEngine(flags: Registry): RollingScorecardPolicyEngine {
    return RollingScorecardPolicyEngine(
        steps = mutableListOf<RollingScorecardPolicyAdjuster>(
            RoundingRollingScorecardPolicyAdjuster(0.05),
            CapRollingScorecardPolicyAdjuster(7904.0),
            FlaggedRollingScorecardPolicyAdjuster(flags, "rolling-scorecard-policy-seasonal", -1.9),
            CountryRollingScorecardPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 1.09, "west" to 1.31, "retail" to 0.88),
            ),
            TieredRollingScorecardPolicyAdjuster(
                mutableListOf<Double>(953.0, 3222.0, 3432.0, 6345.0),
                mutableListOf<Double>(1.03, 0.82, 0.84, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 11642.7,
    )
}
