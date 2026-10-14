package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default incident proposal calculation. */
interface DefaultIncidentProposalAdjuster {
    fun describe(): String
    fun adjust(item: DefaultIncidentProposal, value: Double): Double
}

data class MinimumDefaultIncidentProposalAdjuster(var minimum: Double) : DefaultIncidentProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultIncidentProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultIncidentProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultIncidentProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultIncidentProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultIncidentProposalAdjuster(var cap: Double) : DefaultIncidentProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultIncidentProposal, value: Double): Double = minOf(value, cap)
}

data class DefaultIncidentProposalStopCountOffsetAdjuster(
    var weight: Double,
) : DefaultIncidentProposalAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: DefaultIncidentProposal, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class LabelDefaultIncidentProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultIncidentProposalAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultIncidentProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default incident proposal steps in order and keeps a trace of each. */
data class DefaultIncidentProposalEngine(
    var steps: MutableList<DefaultIncidentProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultIncidentProposal): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultIncidentProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultIncidentProposalEngine(flags: Registry): DefaultIncidentProposalEngine {
    return DefaultIncidentProposalEngine(
        steps = mutableListOf<DefaultIncidentProposalAdjuster>(
            MinimumDefaultIncidentProposalAdjuster(32.3),
            FlaggedDefaultIncidentProposalAdjuster(flags, "default-incident-proposal-rolling", 0.9),
            CapDefaultIncidentProposalAdjuster(6774.0),
            DefaultIncidentProposalStopCountOffsetAdjuster(1.77),
            LabelDefaultIncidentProposalAdjuster(mutableMapOf<String, Double>("south" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 53346.5,
    )
}
