package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive incident proposal calculation. */
interface AdaptiveIncidentProposalAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveIncidentProposal, value: Double): Double
}

data class RoundingAdaptiveIncidentProposalAdjuster(var step: Double) : AdaptiveIncidentProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveIncidentProposal, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveIncidentProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveIncidentProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveIncidentProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveIncidentProposalAdjuster(var percent: Double) : AdaptiveIncidentProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveIncidentProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveIncidentProposalThresholdOffsetAdjuster(
    var weight: Double,
) : AdaptiveIncidentProposalAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: AdaptiveIncidentProposal, value: Double): Double = value + item.threshold * weight
}

/** Runs the adaptive incident proposal steps in order and keeps a trace of each. */
data class AdaptiveIncidentProposalEngine(
    var steps: MutableList<AdaptiveIncidentProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveIncidentProposal): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveIncidentProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveIncidentProposalEngine(flags: Registry): AdaptiveIncidentProposalEngine {
    return AdaptiveIncidentProposalEngine(
        steps = mutableListOf<AdaptiveIncidentProposalAdjuster>(
            RoundingAdaptiveIncidentProposalAdjuster(0.5),
            FlaggedAdaptiveIncidentProposalAdjuster(flags, "adaptive-incident-proposal-strict", 5.1),
            PercentAdaptiveIncidentProposalAdjuster(-21.7),
            AdaptiveIncidentProposalThresholdOffsetAdjuster(0.41),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 36684.0,
    )
}
