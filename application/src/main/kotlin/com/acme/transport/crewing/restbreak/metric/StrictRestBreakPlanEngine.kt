package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.flags.Registry

/** One step of the strict rest break plan calculation. */
interface StrictRestBreakPlanFactor {
    fun describe(): String
    fun adjust(item: StrictRestBreakPlan, value: Double): Double
}

data class CategoryStrictRestBreakPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRestBreakPlanFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: StrictRestBreakPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictRestBreakPlanAdjuster(var minimum: Double) : StrictRestBreakPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRestBreakPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictRestBreakPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRestBreakPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRestBreakPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedStrictRestBreakPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRestBreakPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRestBreakPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict rest break plan steps in order and keeps a trace of each. */
data class StrictRestBreakPlanEngine(
    var steps: MutableList<StrictRestBreakPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRestBreakPlan): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRestBreakPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRestBreakPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRestBreakPlanEngine(flags: Registry): StrictRestBreakPlanEngine {
    return StrictRestBreakPlanEngine(
        steps = mutableListOf<StrictRestBreakPlanFactor>(
            CategoryStrictRestBreakPlanAdjuster(
                mutableMapOf<String, Double>("west" to 1.31, "east" to 1.0, "online" to 1.07),
            ),
            MinimumStrictRestBreakPlanAdjuster(31.8),
            TieredStrictRestBreakPlanAdjuster(
                mutableListOf<Double>(3752.0, 8408.0),
                mutableListOf<Double>(0.92, 0.9),
            ),
            FlaggedStrictRestBreakPlanAdjuster(flags, "strict-rest-break-plan-tiered", -6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 72569.0,
    )
}
