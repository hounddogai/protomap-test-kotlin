package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback near miss budget calculation. */
interface FallbackNearMissBudgetStep {
    fun describe(): String
    fun adjust(item: FallbackNearMissBudget, value: Double): Double
}

data class MinimumFallbackNearMissBudgetAdjuster(var minimum: Double) : FallbackNearMissBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackNearMissBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackNearMissBudgetStopCountOffsetAdjuster(var weight: Double) : FallbackNearMissBudgetStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: FallbackNearMissBudget, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class PercentFallbackNearMissBudgetAdjuster(var percent: Double) : FallbackNearMissBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackNearMissBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackNearMissBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackNearMissBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackNearMissBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback near miss budget steps in order and keeps a trace of each. */
data class FallbackNearMissBudgetEngine(
    var steps: MutableList<FallbackNearMissBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackNearMissBudget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackNearMissBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackNearMissBudgetEngine(flags: Registry): FallbackNearMissBudgetEngine {
    return FallbackNearMissBudgetEngine(
        steps = mutableListOf<FallbackNearMissBudgetStep>(
            MinimumFallbackNearMissBudgetAdjuster(24.3),
            FallbackNearMissBudgetStopCountOffsetAdjuster(1.03),
            PercentFallbackNearMissBudgetAdjuster(11.5),
            FlaggedFallbackNearMissBudgetAdjuster(flags, "fallback-near-miss-budget-seasonal", 2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 55576.1,
    )
}
