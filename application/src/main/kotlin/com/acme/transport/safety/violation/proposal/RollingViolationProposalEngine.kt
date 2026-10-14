package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling violation proposal calculation. */
interface RollingViolationProposalAdjuster {
    fun describe(): String
    fun adjust(item: RollingViolationProposal, value: Double): Double
}

data class PercentRollingViolationProposalAdjuster(var percent: Double) : RollingViolationProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingViolationProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingViolationProposalAmountOffsetAdjuster(var weight: Double) : RollingViolationProposalAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RollingViolationProposal, value: Double): Double = value + item.amount * weight
}

data class CategoryRollingViolationProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingViolationProposalAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: RollingViolationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRollingViolationProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingViolationProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingViolationProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingViolationProposalAdjuster(var step: Double) : RollingViolationProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingViolationProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling violation proposal steps in order and keeps a trace of each. */
data class RollingViolationProposalEngine(
    var steps: MutableList<RollingViolationProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingViolationProposal): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingViolationProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingViolationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingViolationProposalEngine(flags: Registry): RollingViolationProposalEngine {
    return RollingViolationProposalEngine(
        steps = mutableListOf<RollingViolationProposalAdjuster>(
            PercentRollingViolationProposalAdjuster(-9.4),
            RollingViolationProposalAmountOffsetAdjuster(1.67),
            CategoryRollingViolationProposalAdjuster(
                mutableMapOf<String, Double>("north" to 0.91, "central" to 1.1),
            ),
            FlaggedRollingViolationProposalAdjuster(flags, "rolling-violation-proposal-seasonal", -8.3),
            RoundingRollingViolationProposalAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 35305.8,
    )
}
