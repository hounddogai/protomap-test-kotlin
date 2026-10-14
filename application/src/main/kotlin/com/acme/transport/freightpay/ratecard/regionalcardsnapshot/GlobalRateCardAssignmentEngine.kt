package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

/** One step of the global rate card assignment calculation. */
interface GlobalRateCardAssignmentFactor {
    fun describe(): String
    fun adjust(item: GlobalRateCardAssignment, value: Double): Double
}

data class TieredGlobalRateCardAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRateCardAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRateCardAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalRateCardAssignmentAdjuster(var minimum: Double) : GlobalRateCardAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRateCardAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalRateCardAssignmentAdjuster(var cap: Double) : GlobalRateCardAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRateCardAssignment, value: Double): Double = minOf(value, cap)
}

data class NameGlobalRateCardAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRateCardAssignmentFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalRateCardAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalRateCardAssignmentAdjuster(var percent: Double) : GlobalRateCardAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRateCardAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global rate card assignment steps in order and keeps a trace of each. */
data class GlobalRateCardAssignmentEngine(
    var steps: MutableList<GlobalRateCardAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRateCardAssignment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRateCardAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRateCardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRateCardAssignmentEngine(): GlobalRateCardAssignmentEngine {
    return GlobalRateCardAssignmentEngine(
        steps = mutableListOf<GlobalRateCardAssignmentFactor>(
            TieredGlobalRateCardAssignmentAdjuster(
                mutableListOf<Double>(2624.0, 5587.0, 7649.0),
                mutableListOf<Double>(1.12, 1.01, 0.85),
            ),
            MinimumGlobalRateCardAssignmentAdjuster(14.6),
            CapGlobalRateCardAssignmentAdjuster(48726.0),
            NameGlobalRateCardAssignmentAdjuster(
                mutableMapOf<String, Double>("west" to 0.73, "retail" to 1.19, "north" to 1.08),
            ),
            PercentGlobalRateCardAssignmentAdjuster(-15.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 78849.1,
    )
}
