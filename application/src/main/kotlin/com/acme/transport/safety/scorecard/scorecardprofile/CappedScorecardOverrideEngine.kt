package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.flags.Registry

/** One step of the capped scorecard override calculation. */
interface CappedScorecardOverrideFactor {
    fun describe(): String
    fun adjust(item: CappedScorecardOverride, value: Double): Double
}

data class FlaggedCappedScorecardOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedScorecardOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedScorecardOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedScorecardOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedScorecardOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedScorecardOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedScorecardOverrideAdjuster(var minimum: Double) : CappedScorecardOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedScorecardOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped scorecard override steps in order and keeps a trace of each. */
data class CappedScorecardOverrideEngine(
    var steps: MutableList<CappedScorecardOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedScorecardOverride): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedScorecardOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedScorecardOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedScorecardOverrideEngine(flags: Registry): CappedScorecardOverrideEngine {
    return CappedScorecardOverrideEngine(
        steps = mutableListOf<CappedScorecardOverrideFactor>(
            FlaggedCappedScorecardOverrideAdjuster(flags, "capped-scorecard-override-weighted", -3.5),
            TieredCappedScorecardOverrideAdjuster(
                mutableListOf<Double>(1896.0, 3267.0, 5631.0, 9672.0),
                mutableListOf<Double>(1.15, 1.17, 0.98, 0.87),
            ),
            MinimumCappedScorecardOverrideAdjuster(17.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 66103.5,
    )
}
