package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global violation proposal calculation. */
interface GlobalViolationProposalStep {
    fun describe(): String
    fun adjust(item: GlobalViolationProposal, value: Double): Double
}

data class RoundingGlobalViolationProposalAdjuster(var step: Double) : GlobalViolationProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalViolationProposal, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalViolationProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalViolationProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalViolationProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalViolationProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalViolationProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalViolationProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global violation proposal steps in order and keeps a trace of each. */
data class GlobalViolationProposalEngine(
    var steps: MutableList<GlobalViolationProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalViolationProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalViolationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalViolationProposalEngine(flags: Registry): GlobalViolationProposalEngine {
    return GlobalViolationProposalEngine(
        steps = mutableListOf<GlobalViolationProposalStep>(
            RoundingGlobalViolationProposalAdjuster(0.05),
            TieredGlobalViolationProposalAdjuster(
                mutableListOf<Double>(3940.0, 6541.0),
                mutableListOf<Double>(1.11, 0.94),
            ),
            FlaggedGlobalViolationProposalAdjuster(flags, "global-violation-proposal-global", 3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 40422.4,
    )
}
