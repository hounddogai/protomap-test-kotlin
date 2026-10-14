package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the global redelivery milestone calculation. */
interface GlobalRedeliveryMilestoneStep {
    fun describe(): String
    fun adjust(item: GlobalRedeliveryMilestone, value: Double): Double
}

data class FlaggedGlobalRedeliveryMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRedeliveryMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRedeliveryMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalRedeliveryMilestoneVersionOffsetAdjuster(var weight: Double) : GlobalRedeliveryMilestoneStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: GlobalRedeliveryMilestone, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CapGlobalRedeliveryMilestoneAdjuster(var cap: Double) : GlobalRedeliveryMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRedeliveryMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the global redelivery milestone steps in order and keeps a trace of each. */
data class GlobalRedeliveryMilestoneEngine(
    var steps: MutableList<GlobalRedeliveryMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalRedeliveryMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRedeliveryMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRedeliveryMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalRedeliveryMilestoneEngine(flags: Registry): GlobalRedeliveryMilestoneEngine {
    return GlobalRedeliveryMilestoneEngine(
        steps = mutableListOf<GlobalRedeliveryMilestoneStep>(
            FlaggedGlobalRedeliveryMilestoneAdjuster(flags, "global-redelivery-milestone-seasonal", 1.7),
            GlobalRedeliveryMilestoneVersionOffsetAdjuster(1.86),
            CapGlobalRedeliveryMilestoneAdjuster(43337.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 6136.5,
    )
}
