package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance milestone calculation. */
interface ClearanceMilestoneModifier {
    fun describe(): String
    fun adjust(item: ClearanceMilestone, value: Double): Double
}

data class RoundingClearanceMilestoneAdjuster(var step: Double) : ClearanceMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceMilestone, value: Double): Double = roundTo(value, step)
}

data class LabelClearanceMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ClearanceMilestoneModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ClearanceMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ClearanceMilestoneRatioOffsetAdjuster(var weight: Double) : ClearanceMilestoneModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ClearanceMilestone, value: Double): Double = value + item.ratio * weight
}

data class MinimumClearanceMilestoneAdjuster(var minimum: Double) : ClearanceMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the clearance milestone steps in order and keeps a trace of each. */
data class ClearanceMilestoneEngine(
    var steps: MutableList<ClearanceMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ClearanceMilestone): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultClearanceMilestoneEngine(): ClearanceMilestoneEngine {
    return ClearanceMilestoneEngine(
        steps = mutableListOf<ClearanceMilestoneModifier>(
            RoundingClearanceMilestoneAdjuster(0.1),
            LabelClearanceMilestoneAdjuster(mutableMapOf<String, Double>("online" to 1.16, "retail" to 1.03)),
            ClearanceMilestoneRatioOffsetAdjuster(1.49),
            MinimumClearanceMilestoneAdjuster(41.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 44385.8,
    )
}
