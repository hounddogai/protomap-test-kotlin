package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation proposal calculation. */
interface SortationProposalModifier {
    fun describe(): String
    fun adjust(item: SortationProposal, value: Double): Double
}

data class PercentSortationProposalAdjuster(var percent: Double) : SortationProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSortationProposalAdjuster(var step: Double) : SortationProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationProposal, value: Double): Double = roundTo(value, step)
}

data class TitleSortationProposalAdjuster(var factors: MutableMap<String, Double>) : SortationProposalModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: SortationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSortationProposalAdjuster(var cap: Double) : SortationProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumSortationProposalAdjuster(var minimum: Double) : SortationProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSortationProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the sortation proposal steps in order and keeps a trace of each. */
data class SortationProposalEngine(
    var steps: MutableList<SortationProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationProposal): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SortationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationProposalEngine(): SortationProposalEngine {
    return SortationProposalEngine(
        steps = mutableListOf<SortationProposalModifier>(
            PercentSortationProposalAdjuster(18.6),
            RoundingSortationProposalAdjuster(0.25),
            TitleSortationProposalAdjuster(mutableMapOf<String, Double>("north" to 0.74)),
            CapSortationProposalAdjuster(46691.0),
            MinimumSortationProposalAdjuster(20.3),
            TieredSortationProposalAdjuster(
                mutableListOf<Double>(4836.0, 9457.0),
                mutableListOf<Double>(0.92, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 5202.9,
    )
}
