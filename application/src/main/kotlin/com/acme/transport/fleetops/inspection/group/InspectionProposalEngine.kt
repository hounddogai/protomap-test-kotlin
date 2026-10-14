package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.flags.Registry

/** One step of the inspection proposal calculation. */
interface InspectionProposalFactor {
    fun describe(): String
    fun adjust(item: InspectionProposal, value: Double): Double
}

data class FlaggedInspectionProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelInspectionProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionProposalFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: InspectionProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapInspectionProposalAdjuster(var cap: Double) : InspectionProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection proposal steps in order and keeps a trace of each. */
data class InspectionProposalEngine(
    var steps: MutableList<InspectionProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionProposalEngine(flags: Registry): InspectionProposalEngine {
    return InspectionProposalEngine(
        steps = mutableListOf<InspectionProposalFactor>(
            FlaggedInspectionProposalAdjuster(flags, "inspection-proposal-legacy", 0.8),
            ChannelInspectionProposalAdjuster(mutableMapOf<String, Double>("south" to 0.88)),
            CapInspectionProposalAdjuster(36017.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 40444.4,
    )
}
