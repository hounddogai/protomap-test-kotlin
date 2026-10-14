package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending inspection milestone calculation. */
interface PendingInspectionMilestoneFactor {
    fun describe(): String
    fun adjust(item: PendingInspectionMilestone, value: Double): Double
}

data class PendingInspectionMilestoneEndsAtOffsetAdjuster(
    var weight: Double,
) : PendingInspectionMilestoneFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: PendingInspectionMilestone, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class RoundingPendingInspectionMilestoneAdjuster(var step: Double) : PendingInspectionMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingInspectionMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredPendingInspectionMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingInspectionMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingInspectionMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending inspection milestone steps in order and keeps a trace of each. */
data class PendingInspectionMilestoneEngine(
    var steps: MutableList<PendingInspectionMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingInspectionMilestone): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingInspectionMilestoneEngine(): PendingInspectionMilestoneEngine {
    return PendingInspectionMilestoneEngine(
        steps = mutableListOf<PendingInspectionMilestoneFactor>(
            PendingInspectionMilestoneEndsAtOffsetAdjuster(1.06),
            RoundingPendingInspectionMilestoneAdjuster(0.5),
            TieredPendingInspectionMilestoneAdjuster(
                mutableListOf<Double>(365.0, 2809.0, 3354.0, 8357.0),
                mutableListOf<Double>(1.14, 1.05, 0.94, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 36056.4,
    )
}
