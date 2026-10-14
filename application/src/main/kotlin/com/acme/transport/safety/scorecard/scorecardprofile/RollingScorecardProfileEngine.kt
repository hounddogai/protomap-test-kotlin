package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.flags.Registry

/** One step of the rolling scorecard profile calculation. */
interface RollingScorecardProfileStep {
    fun describe(): String
    fun adjust(item: RollingScorecardProfile, value: Double): Double
}

data class FlaggedRollingScorecardProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingScorecardProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingScorecardProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingScorecardProfileAdjuster(var cap: Double) : RollingScorecardProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingScorecardProfile, value: Double): Double = minOf(value, cap)
}

data class RollingScorecardProfileQuantityOffsetAdjuster(var weight: Double) : RollingScorecardProfileStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RollingScorecardProfile, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class TieredRollingScorecardProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingScorecardProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingScorecardProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling scorecard profile steps in order and keeps a trace of each. */
data class RollingScorecardProfileEngine(
    var steps: MutableList<RollingScorecardProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingScorecardProfile): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingScorecardProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingScorecardProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingScorecardProfileEngine(flags: Registry): RollingScorecardProfileEngine {
    return RollingScorecardProfileEngine(
        steps = mutableListOf<RollingScorecardProfileStep>(
            FlaggedRollingScorecardProfileAdjuster(flags, "rolling-scorecard-profile-strict", -6.5),
            CapRollingScorecardProfileAdjuster(21870.0),
            RollingScorecardProfileQuantityOffsetAdjuster(1.17),
            TieredRollingScorecardProfileAdjuster(
                mutableListOf<Double>(606.0, 1720.0),
                mutableListOf<Double>(1.04, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 35798.2,
    )
}
