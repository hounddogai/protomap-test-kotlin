package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break proposal calculation. */
interface RestBreakProposalStep {
    fun describe(): String
    fun adjust(item: RestBreakProposal, value: Double): Double
}

data class RoundingRestBreakProposalAdjuster(var step: Double) : RestBreakProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakProposal, value: Double): Double = roundTo(value, step)
}

data class RestBreakProposalAmountOffsetAdjuster(var weight: Double) : RestBreakProposalStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RestBreakProposal, value: Double): Double = value + item.amount * weight
}

data class CapRestBreakProposalAdjuster(var cap: Double) : RestBreakProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakProposal, value: Double): Double = minOf(value, cap)
}

data class FlaggedRestBreakProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRestBreakProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rest break proposal steps in order and keeps a trace of each. */
data class RestBreakProposalEngine(
    var steps: MutableList<RestBreakProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakProposal): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakProposalEngine(flags: Registry): RestBreakProposalEngine {
    return RestBreakProposalEngine(
        steps = mutableListOf<RestBreakProposalStep>(
            RoundingRestBreakProposalAdjuster(0.1),
            RestBreakProposalAmountOffsetAdjuster(1.84),
            CapRestBreakProposalAdjuster(4016.0),
            FlaggedRestBreakProposalAdjuster(flags, "rest-break-proposal-rolling", -2.9),
            TieredRestBreakProposalAdjuster(
                mutableListOf<Double>(3502.0, 4623.0),
                mutableListOf<Double>(1.08, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 53011.8,
    )
}
