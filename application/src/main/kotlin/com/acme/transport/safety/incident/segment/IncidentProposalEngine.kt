package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.flags.Registry

/** One step of the incident proposal calculation. */
interface IncidentProposalStep {
    fun describe(): String
    fun adjust(item: IncidentProposal, value: Double): Double
}

data class IncidentProposalCeilingOffsetAdjuster(var weight: Double) : IncidentProposalStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: IncidentProposal, value: Double): Double = value + item.ceiling * weight
}

data class LabelIncidentProposalAdjuster(var factors: MutableMap<String, Double>) : IncidentProposalStep {
    override fun describe(): String = "by label"

    override fun adjust(item: IncidentProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapIncidentProposalAdjuster(var cap: Double) : IncidentProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentProposal, value: Double): Double = minOf(value, cap)
}

data class TieredIncidentProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumIncidentProposalAdjuster(var minimum: Double) : IncidentProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIncidentProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the incident proposal steps in order and keeps a trace of each. */
data class IncidentProposalEngine(
    var steps: MutableList<IncidentProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentProposal): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentProposalEngine(flags: Registry): IncidentProposalEngine {
    return IncidentProposalEngine(
        steps = mutableListOf<IncidentProposalStep>(
            IncidentProposalCeilingOffsetAdjuster(0.21),
            LabelIncidentProposalAdjuster(mutableMapOf<String, Double>("central" to 1.33, "north" to 1.22)),
            CapIncidentProposalAdjuster(25951.0),
            TieredIncidentProposalAdjuster(
                mutableListOf<Double>(546.0, 4754.0, 6844.0, 9696.0),
                mutableListOf<Double>(1.11, 1.18, 0.9, 1.03),
            ),
            MinimumIncidentProposalAdjuster(0.0),
            FlaggedIncidentProposalAdjuster(flags, "incident-proposal-capped", 4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 29297.9,
    )
}
