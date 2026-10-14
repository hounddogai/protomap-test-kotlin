package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute plan calculation. */
interface DisputePlanAdjuster {
    fun describe(): String
    fun adjust(item: DisputePlan, value: Double): Double
}

data class CapDisputePlanAdjuster(var cap: Double) : DisputePlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputePlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedDisputePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputePlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotDisputePlanAdjuster(var factors: MutableMap<String, Double>) : DisputePlanAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: DisputePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDisputePlanAdjuster(var minimum: Double) : DisputePlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDisputePlanAdjuster(var step: Double) : DisputePlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputePlan, value: Double): Double = roundTo(value, step)
}

/** Runs the dispute plan steps in order and keeps a trace of each. */
data class DisputePlanEngine(
    var steps: MutableList<DisputePlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputePlan): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputePlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DisputePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputePlanEngine(flags: Registry): DisputePlanEngine {
    return DisputePlanEngine(
        steps = mutableListOf<DisputePlanAdjuster>(
            CapDisputePlanAdjuster(5268.0),
            FlaggedDisputePlanAdjuster(flags, "dispute-plan-strict", 5.4),
            DepotDisputePlanAdjuster(mutableMapOf<String, Double>("online" to 0.98)),
            MinimumDisputePlanAdjuster(43.1),
            RoundingDisputePlanAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 90749.3,
    )
}
