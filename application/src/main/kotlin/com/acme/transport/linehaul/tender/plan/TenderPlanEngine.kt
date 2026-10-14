package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the tender plan calculation. */
interface TenderPlanAdjuster {
    fun describe(): String
    fun adjust(item: TenderPlan, value: Double): Double
}

data class RoundingTenderPlanAdjuster(var step: Double) : TenderPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderPlan, value: Double): Double = roundTo(value, step)
}

data class TenderPlanStartsAtOffsetAdjuster(var weight: Double) : TenderPlanAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: TenderPlan, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class LaneTenderPlanAdjuster(var factors: MutableMap<String, Double>) : TenderPlanAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TenderPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTenderPlanAdjuster(var cap: Double) : TenderPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderPlan, value: Double): Double = minOf(value, cap)
}

data class PercentTenderPlanAdjuster(var percent: Double) : TenderPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTenderPlanAdjuster(var minimum: Double) : TenderPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tender plan steps in order and keeps a trace of each. */
data class TenderPlanEngine(
    var steps: MutableList<TenderPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderPlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TenderPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderPlanEngine(): TenderPlanEngine {
    return TenderPlanEngine(
        steps = mutableListOf<TenderPlanAdjuster>(
            RoundingTenderPlanAdjuster(0.25),
            TenderPlanStartsAtOffsetAdjuster(1.85),
            LaneTenderPlanAdjuster(mutableMapOf<String, Double>("east" to 1.31, "online" to 1.13)),
            CapTenderPlanAdjuster(39925.0),
            PercentTenderPlanAdjuster(-9.6),
            MinimumTenderPlanAdjuster(6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 92323.9,
    )
}
