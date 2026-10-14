package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the capped tire plan calculation. */
interface CappedTirePlanStep {
    fun describe(): String
    fun adjust(item: CappedTirePlan, value: Double): Double
}

data class CappedTirePlanEndsAtOffsetAdjuster(var weight: Double) : CappedTirePlanStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CappedTirePlan, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class MinimumCappedTirePlanAdjuster(var minimum: Double) : CappedTirePlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTirePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedTirePlanAdjuster(var percent: Double) : CappedTirePlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTirePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedTirePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTirePlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTirePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedTirePlanAdjuster(var cap: Double) : CappedTirePlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTirePlan, value: Double): Double = minOf(value, cap)
}

data class TitleCappedTirePlanAdjuster(var factors: MutableMap<String, Double>) : CappedTirePlanStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedTirePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped tire plan steps in order and keeps a trace of each. */
data class CappedTirePlanEngine(
    var steps: MutableList<CappedTirePlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTirePlan): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedTirePlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedTirePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTirePlanEngine(flags: Registry): CappedTirePlanEngine {
    return CappedTirePlanEngine(
        steps = mutableListOf<CappedTirePlanStep>(
            CappedTirePlanEndsAtOffsetAdjuster(0.39),
            MinimumCappedTirePlanAdjuster(47.9),
            PercentCappedTirePlanAdjuster(13.2),
            FlaggedCappedTirePlanAdjuster(flags, "capped-tire-plan-scheduled", -1.6),
            CapCappedTirePlanAdjuster(13552.0),
            TitleCappedTirePlanAdjuster(
                mutableMapOf<String, Double>("west" to 1.4, "east" to 1.0, "north" to 0.71),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 70437.1,
    )
}
