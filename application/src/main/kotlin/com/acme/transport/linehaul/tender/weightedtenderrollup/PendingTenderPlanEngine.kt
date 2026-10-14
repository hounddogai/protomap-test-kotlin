package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending tender plan calculation. */
interface PendingTenderPlanStep {
    fun describe(): String
    fun adjust(item: PendingTenderPlan, value: Double): Double
}

data class PendingTenderPlanRevisionOffsetAdjuster(var weight: Double) : PendingTenderPlanStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PendingTenderPlan, value: Double): Double = value + item.revision.toDouble() * weight
}

data class CapPendingTenderPlanAdjuster(var cap: Double) : PendingTenderPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTenderPlan, value: Double): Double = minOf(value, cap)
}

data class UnitPendingTenderPlanAdjuster(var factors: MutableMap<String, Double>) : PendingTenderPlanStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingTenderPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingTenderPlanAdjuster(var minimum: Double) : PendingTenderPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTenderPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingTenderPlanAdjuster(var step: Double) : PendingTenderPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTenderPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the pending tender plan steps in order and keeps a trace of each. */
data class PendingTenderPlanEngine(
    var steps: MutableList<PendingTenderPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingTenderPlan): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTenderPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTenderPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingTenderPlanEngine(): PendingTenderPlanEngine {
    return PendingTenderPlanEngine(
        steps = mutableListOf<PendingTenderPlanStep>(
            PendingTenderPlanRevisionOffsetAdjuster(0.41),
            CapPendingTenderPlanAdjuster(18930.0),
            UnitPendingTenderPlanAdjuster(
                mutableMapOf<String, Double>("east" to 1.11, "retail" to 0.72, "south" to 0.73),
            ),
            MinimumPendingTenderPlanAdjuster(26.4),
            RoundingPendingTenderPlanAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 80869.6,
    )
}
