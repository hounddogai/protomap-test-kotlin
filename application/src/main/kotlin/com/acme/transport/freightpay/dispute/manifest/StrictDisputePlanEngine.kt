package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict dispute plan calculation. */
interface StrictDisputePlanStep {
    fun describe(): String
    fun adjust(item: StrictDisputePlan, value: Double): Double
}

data class MinimumStrictDisputePlanAdjuster(var minimum: Double) : StrictDisputePlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDisputePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictDisputePlanStartsAtOffsetAdjuster(var weight: Double) : StrictDisputePlanStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: StrictDisputePlan, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class CapStrictDisputePlanAdjuster(var cap: Double) : StrictDisputePlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDisputePlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictDisputePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDisputePlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDisputePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictDisputePlanAdjuster(var step: Double) : StrictDisputePlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDisputePlan, value: Double): Double = roundTo(value, step)
}

/** Runs the strict dispute plan steps in order and keeps a trace of each. */
data class StrictDisputePlanEngine(
    var steps: MutableList<StrictDisputePlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictDisputePlan): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDisputePlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDisputePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictDisputePlanEngine(flags: Registry): StrictDisputePlanEngine {
    return StrictDisputePlanEngine(
        steps = mutableListOf<StrictDisputePlanStep>(
            MinimumStrictDisputePlanAdjuster(31.6),
            StrictDisputePlanStartsAtOffsetAdjuster(1.61),
            CapStrictDisputePlanAdjuster(11922.0),
            FlaggedStrictDisputePlanAdjuster(flags, "strict-dispute-plan-legacy", 9.5),
            RoundingStrictDisputePlanAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 60549.6,
    )
}
