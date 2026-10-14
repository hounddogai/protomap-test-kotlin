package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan proposal calculation. */
interface LanePlanProposalAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanProposal, value: Double): Double
}

data class CapLanePlanProposalAdjuster(var cap: Double) : LanePlanProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanProposal, value: Double): Double = minOf(value, cap)
}

data class TieredLanePlanProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLanePlanProposalAdjuster(var step: Double) : LanePlanProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanProposal, value: Double): Double = roundTo(value, step)
}

data class FlaggedLanePlanProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lane plan proposal steps in order and keeps a trace of each. */
data class LanePlanProposalEngine(
    var steps: MutableList<LanePlanProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanProposal): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanProposalEngine(flags: Registry): LanePlanProposalEngine {
    return LanePlanProposalEngine(
        steps = mutableListOf<LanePlanProposalAdjuster>(
            CapLanePlanProposalAdjuster(15072.0),
            TieredLanePlanProposalAdjuster(
                mutableListOf<Double>(2349.0, 7185.0, 9696.0),
                mutableListOf<Double>(1.1, 0.95, 0.86),
            ),
            RoundingLanePlanProposalAdjuster(0.1),
            FlaggedLanePlanProposalAdjuster(flags, "lane-plan-proposal-scheduled", -1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 48985.0,
    )
}
