package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.flags.Registry

/** One step of the weighted scorecard profile calculation. */
interface WeightedScorecardProfileStep {
    fun describe(): String
    fun adjust(item: WeightedScorecardProfile, value: Double): Double
}

data class TieredWeightedScorecardProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedScorecardProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedScorecardProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedScorecardProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedScorecardProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedScorecardProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedScorecardProfileAdjuster(var cap: Double) : WeightedScorecardProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedScorecardProfile, value: Double): Double = minOf(value, cap)
}

data class WeightedScorecardProfileRetriesOffsetAdjuster(var weight: Double) : WeightedScorecardProfileStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: WeightedScorecardProfile, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class PercentWeightedScorecardProfileAdjuster(var percent: Double) : WeightedScorecardProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedScorecardProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedScorecardProfileAdjuster(var minimum: Double) : WeightedScorecardProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedScorecardProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted scorecard profile steps in order and keeps a trace of each. */
data class WeightedScorecardProfileEngine(
    var steps: MutableList<WeightedScorecardProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedScorecardProfile): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedScorecardProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedScorecardProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedScorecardProfileEngine(flags: Registry): WeightedScorecardProfileEngine {
    return WeightedScorecardProfileEngine(
        steps = mutableListOf<WeightedScorecardProfileStep>(
            TieredWeightedScorecardProfileAdjuster(
                mutableListOf<Double>(7005.0, 7555.0, 8232.0, 9733.0),
                mutableListOf<Double>(0.85, 1.08, 1.19, 0.85),
            ),
            FlaggedWeightedScorecardProfileAdjuster(flags, "weighted-scorecard-profile-default", 3.2),
            CapWeightedScorecardProfileAdjuster(2109.0),
            WeightedScorecardProfileRetriesOffsetAdjuster(0.02),
            PercentWeightedScorecardProfileAdjuster(10.5),
            MinimumWeightedScorecardProfileAdjuster(7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 58661.4,
    )
}
