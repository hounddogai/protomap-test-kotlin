package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.flags.Registry

/** One step of the lenient parcel proposal calculation. */
interface LenientParcelProposalStep {
    fun describe(): String
    fun adjust(item: LenientParcelProposal, value: Double): Double
}

data class MinimumLenientParcelProposalAdjuster(var minimum: Double) : LenientParcelProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientParcelProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientParcelProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientParcelProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientParcelProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientParcelProposalAdjuster(var percent: Double) : LenientParcelProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientParcelProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientParcelProposalAdjuster(var cap: Double) : LenientParcelProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientParcelProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient parcel proposal steps in order and keeps a trace of each. */
data class LenientParcelProposalEngine(
    var steps: MutableList<LenientParcelProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientParcelProposal): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientParcelProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientParcelProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientParcelProposalEngine(flags: Registry): LenientParcelProposalEngine {
    return LenientParcelProposalEngine(
        steps = mutableListOf<LenientParcelProposalStep>(
            MinimumLenientParcelProposalAdjuster(5.5),
            FlaggedLenientParcelProposalAdjuster(flags, "lenient-parcel-proposal-batched", -3.5),
            PercentLenientParcelProposalAdjuster(6.7),
            CapLenientParcelProposalAdjuster(16996.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 54736.8,
    )
}
