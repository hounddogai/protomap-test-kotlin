package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled fuel burn proposal calculation. */
interface ScheduledFuelBurnProposalStep {
    fun describe(): String
    fun adjust(item: ScheduledFuelBurnProposal, value: Double): Double
}

data class FlaggedScheduledFuelBurnProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledFuelBurnProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledFuelBurnProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledFuelBurnProposalAdjuster(var step: Double) : ScheduledFuelBurnProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledFuelBurnProposal, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledFuelBurnProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledFuelBurnProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledFuelBurnProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledFuelBurnProposalAdjuster(var cap: Double) : ScheduledFuelBurnProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledFuelBurnProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled fuel burn proposal steps in order and keeps a trace of each. */
data class ScheduledFuelBurnProposalEngine(
    var steps: MutableList<ScheduledFuelBurnProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledFuelBurnProposal): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledFuelBurnProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledFuelBurnProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledFuelBurnProposalEngine(flags: Registry): ScheduledFuelBurnProposalEngine {
    return ScheduledFuelBurnProposalEngine(
        steps = mutableListOf<ScheduledFuelBurnProposalStep>(
            FlaggedScheduledFuelBurnProposalAdjuster(flags, "scheduled-fuel-burn-proposal-manual", 6.9),
            RoundingScheduledFuelBurnProposalAdjuster(0.25),
            TieredScheduledFuelBurnProposalAdjuster(
                mutableListOf<Double>(4841.0, 7265.0, 9285.0),
                mutableListOf<Double>(0.85, 1.18, 1.08),
            ),
            CapScheduledFuelBurnProposalAdjuster(20205.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 51171.5,
    )
}
