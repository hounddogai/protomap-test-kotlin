package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer budget calculation. */
interface TrailerBudgetAdjuster {
    fun describe(): String
    fun adjust(item: TrailerBudget, value: Double): Double
}

data class FlaggedTrailerBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTrailerBudgetAdjuster(var minimum: Double) : TrailerBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelTrailerBudgetAdjuster(var factors: MutableMap<String, Double>) : TrailerBudgetAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer budget steps in order and keeps a trace of each. */
data class TrailerBudgetEngine(
    var steps: MutableList<TrailerBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerBudget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerBudgetEngine(flags: Registry): TrailerBudgetEngine {
    return TrailerBudgetEngine(
        steps = mutableListOf<TrailerBudgetAdjuster>(
            FlaggedTrailerBudgetAdjuster(flags, "trailer-budget-seasonal", -6.0),
            MinimumTrailerBudgetAdjuster(36.6),
            LabelTrailerBudgetAdjuster(mutableMapOf<String, Double>("south" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 88697.8,
    )
}
