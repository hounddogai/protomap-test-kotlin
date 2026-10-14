package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial milestone calculation. */
interface AccessorialMilestoneModifier {
    fun describe(): String
    fun adjust(item: AccessorialMilestone, value: Double): Double
}

data class AccessorialMilestoneRetriesOffsetAdjuster(var weight: Double) : AccessorialMilestoneModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AccessorialMilestone, value: Double): Double = value + item.retries.toDouble() * weight
}

data class FlaggedAccessorialMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAccessorialMilestoneAdjuster(var step: Double) : AccessorialMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialMilestone, value: Double): Double = roundTo(value, step)
}

data class UnitAccessorialMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialMilestoneModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: AccessorialMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAccessorialMilestoneAdjuster(var cap: Double) : AccessorialMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumAccessorialMilestoneAdjuster(var minimum: Double) : AccessorialMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial milestone steps in order and keeps a trace of each. */
data class AccessorialMilestoneEngine(
    var steps: MutableList<AccessorialMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialMilestone): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialMilestoneEngine(flags: Registry): AccessorialMilestoneEngine {
    return AccessorialMilestoneEngine(
        steps = mutableListOf<AccessorialMilestoneModifier>(
            AccessorialMilestoneRetriesOffsetAdjuster(1.64),
            FlaggedAccessorialMilestoneAdjuster(flags, "accessorial-milestone-adaptive", 2.7),
            RoundingAccessorialMilestoneAdjuster(1.0),
            UnitAccessorialMilestoneAdjuster(mutableMapOf<String, Double>("north" to 1.2, "south" to 1.12)),
            CapAccessorialMilestoneAdjuster(23318.0),
            MinimumAccessorialMilestoneAdjuster(42.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 82994.8,
    )
}
