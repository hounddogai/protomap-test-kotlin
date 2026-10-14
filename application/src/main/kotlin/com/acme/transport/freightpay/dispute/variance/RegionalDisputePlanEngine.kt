package com.acme.transport.freightpay.dispute.variance

/** One step of the regional dispute plan calculation. */
interface RegionalDisputePlanAdjuster {
    fun describe(): String
    fun adjust(item: RegionalDisputePlan, value: Double): Double
}

data class CapRegionalDisputePlanAdjuster(var cap: Double) : RegionalDisputePlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDisputePlan, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalDisputePlanAdjuster(var minimum: Double) : RegionalDisputePlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDisputePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalDisputePlanAttemptsOffsetAdjuster(var weight: Double) : RegionalDisputePlanAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RegionalDisputePlan, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the regional dispute plan steps in order and keeps a trace of each. */
data class RegionalDisputePlanEngine(
    var steps: MutableList<RegionalDisputePlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalDisputePlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDisputePlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDisputePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalDisputePlanEngine(): RegionalDisputePlanEngine {
    return RegionalDisputePlanEngine(
        steps = mutableListOf<RegionalDisputePlanAdjuster>(
            CapRegionalDisputePlanAdjuster(41973.0),
            MinimumRegionalDisputePlanAdjuster(20.4),
            RegionalDisputePlanAttemptsOffsetAdjuster(0.94),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 65700.9,
    )
}
