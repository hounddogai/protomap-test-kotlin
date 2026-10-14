package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled violation proposal calculation. */
interface ScheduledViolationProposalAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledViolationProposal, value: Double): Double
}

data class CapScheduledViolationProposalAdjuster(var cap: Double) : ScheduledViolationProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledViolationProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledViolationProposalAdjuster(var minimum: Double) : ScheduledViolationProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledViolationProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleScheduledViolationProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledViolationProposalAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledViolationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledViolationProposalAdjuster(var step: Double) : ScheduledViolationProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledViolationProposal, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledViolationProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledViolationProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledViolationProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledViolationProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledViolationProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledViolationProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled violation proposal steps in order and keeps a trace of each. */
data class ScheduledViolationProposalEngine(
    var steps: MutableList<ScheduledViolationProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledViolationProposal): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledViolationProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledViolationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledViolationProposalEngine(flags: Registry): ScheduledViolationProposalEngine {
    return ScheduledViolationProposalEngine(
        steps = mutableListOf<ScheduledViolationProposalAdjuster>(
            CapScheduledViolationProposalAdjuster(42621.0),
            MinimumScheduledViolationProposalAdjuster(34.5),
            TitleScheduledViolationProposalAdjuster(
                mutableMapOf<String, Double>("central" to 1.04, "east" to 1.34),
            ),
            RoundingScheduledViolationProposalAdjuster(5.0),
            TieredScheduledViolationProposalAdjuster(
                mutableListOf<Double>(987.0, 8139.0, 8729.0),
                mutableListOf<Double>(1.18, 0.81, 0.98),
            ),
            FlaggedScheduledViolationProposalAdjuster(flags, "scheduled-violation-proposal-default", -6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 32144.6,
    )
}
