package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect proposal calculation. */
interface DefectProposalModifier {
    fun describe(): String
    fun adjust(item: DefectProposal, value: Double): Double
}

data class TieredDefectProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryDefectProposalAdjuster(var factors: MutableMap<String, Double>) : DefectProposalModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: DefectProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefectProposalAdjuster(var percent: Double) : DefectProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefectProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefectProposalAdjuster(var minimum: Double) : DefectProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefectProposalMaximumOffsetAdjuster(var weight: Double) : DefectProposalModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DefectProposal, value: Double): Double = value + item.maximum * weight
}

/** Runs the defect proposal steps in order and keeps a trace of each. */
data class DefectProposalEngine(
    var steps: MutableList<DefectProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectProposal): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectProposalEngine(flags: Registry): DefectProposalEngine {
    return DefectProposalEngine(
        steps = mutableListOf<DefectProposalModifier>(
            TieredDefectProposalAdjuster(
                mutableListOf<Double>(5087.0, 8276.0, 9200.0),
                mutableListOf<Double>(0.86, 0.84, 0.99),
            ),
            CategoryDefectProposalAdjuster(
                mutableMapOf<String, Double>("central" to 0.98, "east" to 1.26, "south" to 1.15),
            ),
            PercentDefectProposalAdjuster(-14.2),
            FlaggedDefectProposalAdjuster(flags, "defect-proposal-global", 9.2),
            MinimumDefectProposalAdjuster(20.8),
            DefectProposalMaximumOffsetAdjuster(1.89),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 32709.6,
    )
}
