package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery milestone calculation. */
interface RedeliveryMilestoneModifier {
    fun describe(): String
    fun adjust(item: RedeliveryMilestone, value: Double): Double
}

data class RedeliveryMilestoneRevisionOffsetAdjuster(var weight: Double) : RedeliveryMilestoneModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: RedeliveryMilestone, value: Double): Double = value + item.revision.toDouble() * weight
}

data class FlaggedRedeliveryMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRedeliveryMilestoneAdjuster(var cap: Double) : RedeliveryMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingRedeliveryMilestoneAdjuster(var step: Double) : RedeliveryMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentRedeliveryMilestoneAdjuster(var percent: Double) : RedeliveryMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameRedeliveryMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryMilestoneModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RedeliveryMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the redelivery milestone steps in order and keeps a trace of each. */
data class RedeliveryMilestoneEngine(
    var steps: MutableList<RedeliveryMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryMilestone): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryMilestoneEngine(flags: Registry): RedeliveryMilestoneEngine {
    return RedeliveryMilestoneEngine(
        steps = mutableListOf<RedeliveryMilestoneModifier>(
            RedeliveryMilestoneRevisionOffsetAdjuster(1.29),
            FlaggedRedeliveryMilestoneAdjuster(flags, "redelivery-milestone-primary", 9.3),
            CapRedeliveryMilestoneAdjuster(37940.0),
            RoundingRedeliveryMilestoneAdjuster(5.0),
            PercentRedeliveryMilestoneAdjuster(4.1),
            NameRedeliveryMilestoneAdjuster(
                mutableMapOf<String, Double>("central" to 1.37, "south" to 0.73, "east" to 1.3),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 96642.5,
    )
}
