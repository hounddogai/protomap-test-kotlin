package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the backhaul milestone calculation. */
interface BackhaulMilestoneModifier {
    fun describe(): String
    fun adjust(item: BackhaulMilestone, value: Double): Double
}

data class PercentBackhaulMilestoneAdjuster(var percent: Double) : BackhaulMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotBackhaulMilestoneAdjuster(var factors: MutableMap<String, Double>) : BackhaulMilestoneModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: BackhaulMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBackhaulMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BackhaulMilestoneEndsAtOffsetAdjuster(var weight: Double) : BackhaulMilestoneModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: BackhaulMilestone, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the backhaul milestone steps in order and keeps a trace of each. */
data class BackhaulMilestoneEngine(
    var steps: MutableList<BackhaulMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulMilestone): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulMilestoneEngine(flags: Registry): BackhaulMilestoneEngine {
    return BackhaulMilestoneEngine(
        steps = mutableListOf<BackhaulMilestoneModifier>(
            PercentBackhaulMilestoneAdjuster(-18.9),
            DepotBackhaulMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 1.07, "north" to 0.85, "online" to 0.93),
            ),
            FlaggedBackhaulMilestoneAdjuster(flags, "backhaul-milestone-legacy", 2.4),
            BackhaulMilestoneEndsAtOffsetAdjuster(1.43),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 70222.0,
    )
}
