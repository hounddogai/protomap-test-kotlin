package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the declaration proposal calculation. */
interface DeclarationProposalFactor {
    fun describe(): String
    fun adjust(item: DeclarationProposal, value: Double): Double
}

data class FlaggedDeclarationProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DeclarationProposalDistanceKmOffsetAdjuster(var weight: Double) : DeclarationProposalFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DeclarationProposal, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingDeclarationProposalAdjuster(var step: Double) : DeclarationProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationProposal, value: Double): Double = roundTo(value, step)
}

data class NameDeclarationProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationProposalFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: DeclarationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDeclarationProposalAdjuster(var percent: Double) : DeclarationProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the declaration proposal steps in order and keeps a trace of each. */
data class DeclarationProposalEngine(
    var steps: MutableList<DeclarationProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationProposal): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationProposalEngine(flags: Registry): DeclarationProposalEngine {
    return DeclarationProposalEngine(
        steps = mutableListOf<DeclarationProposalFactor>(
            FlaggedDeclarationProposalAdjuster(flags, "declaration-proposal-weighted", 9.0),
            DeclarationProposalDistanceKmOffsetAdjuster(0.16),
            RoundingDeclarationProposalAdjuster(1.0),
            NameDeclarationProposalAdjuster(
                mutableMapOf<String, Double>("west" to 1.19, "north" to 1.3, "central" to 0.8),
            ),
            PercentDeclarationProposalAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 72043.1,
    )
}
