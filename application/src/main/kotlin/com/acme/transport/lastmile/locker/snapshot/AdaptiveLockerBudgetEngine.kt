package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive locker budget calculation. */
interface AdaptiveLockerBudgetModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLockerBudget, value: Double): Double
}

data class PercentAdaptiveLockerBudgetAdjuster(var percent: Double) : AdaptiveLockerBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLockerBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveLockerBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLockerBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLockerBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveLockerBudgetAdjuster(var step: Double) : AdaptiveLockerBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLockerBudget, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveLockerBudgetAdjuster(var cap: Double) : AdaptiveLockerBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLockerBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveLockerBudgetAdjuster(var minimum: Double) : AdaptiveLockerBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLockerBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive locker budget steps in order and keeps a trace of each. */
data class AdaptiveLockerBudgetEngine(
    var steps: MutableList<AdaptiveLockerBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveLockerBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLockerBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveLockerBudgetEngine(flags: Registry): AdaptiveLockerBudgetEngine {
    return AdaptiveLockerBudgetEngine(
        steps = mutableListOf<AdaptiveLockerBudgetModifier>(
            PercentAdaptiveLockerBudgetAdjuster(-7.0),
            FlaggedAdaptiveLockerBudgetAdjuster(flags, "adaptive-locker-budget-rolling", 5.3),
            RoundingAdaptiveLockerBudgetAdjuster(0.1),
            CapAdaptiveLockerBudgetAdjuster(28092.0),
            MinimumAdaptiveLockerBudgetAdjuster(6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 82415.6,
    )
}
