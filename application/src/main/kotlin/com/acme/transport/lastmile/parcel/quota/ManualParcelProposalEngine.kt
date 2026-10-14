package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual parcel proposal calculation. */
interface ManualParcelProposalStep {
    fun describe(): String
    fun adjust(item: ManualParcelProposal, value: Double): Double
}

data class LabelManualParcelProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualParcelProposalStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualParcelProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualParcelProposalRatioOffsetAdjuster(var weight: Double) : ManualParcelProposalStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ManualParcelProposal, value: Double): Double = value + item.ratio * weight
}

data class RoundingManualParcelProposalAdjuster(var step: Double) : ManualParcelProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualParcelProposal, value: Double): Double = roundTo(value, step)
}

data class CapManualParcelProposalAdjuster(var cap: Double) : ManualParcelProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualParcelProposal, value: Double): Double = minOf(value, cap)
}

data class PercentManualParcelProposalAdjuster(var percent: Double) : ManualParcelProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualParcelProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualParcelProposalAdjuster(var minimum: Double) : ManualParcelProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualParcelProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual parcel proposal steps in order and keeps a trace of each. */
data class ManualParcelProposalEngine(
    var steps: MutableList<ManualParcelProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualParcelProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualParcelProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualParcelProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualParcelProposalEngine(): ManualParcelProposalEngine {
    return ManualParcelProposalEngine(
        steps = mutableListOf<ManualParcelProposalStep>(
            LabelManualParcelProposalAdjuster(mutableMapOf<String, Double>("north" to 0.82, "retail" to 1.39)),
            ManualParcelProposalRatioOffsetAdjuster(0.86),
            RoundingManualParcelProposalAdjuster(5.0),
            CapManualParcelProposalAdjuster(29609.0),
            PercentManualParcelProposalAdjuster(-14.2),
            MinimumManualParcelProposalAdjuster(24.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 38898.8,
    )
}
