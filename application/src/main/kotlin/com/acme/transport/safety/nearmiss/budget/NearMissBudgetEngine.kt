package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss budget calculation. */
interface NearMissBudgetAdjuster {
    fun describe(): String
    fun adjust(item: NearMissBudget, value: Double): Double
}

data class MinimumNearMissBudgetAdjuster(var minimum: Double) : NearMissBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentNearMissBudgetAdjuster(var percent: Double) : NearMissBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedNearMissBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the near miss budget steps in order and keeps a trace of each. */
data class NearMissBudgetEngine(
    var steps: MutableList<NearMissBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NearMissBudget): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNearMissBudgetEngine(flags: Registry): NearMissBudgetEngine {
    return NearMissBudgetEngine(
        steps = mutableListOf<NearMissBudgetAdjuster>(
            MinimumNearMissBudgetAdjuster(8.6),
            PercentNearMissBudgetAdjuster(-17.9),
            FlaggedNearMissBudgetAdjuster(flags, "near-miss-budget-scheduled", 1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 18367.6,
    )
}
