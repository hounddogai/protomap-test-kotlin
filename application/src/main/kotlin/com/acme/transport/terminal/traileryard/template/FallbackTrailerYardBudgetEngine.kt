package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback trailer yard budget calculation. */
interface FallbackTrailerYardBudgetFactor {
    fun describe(): String
    fun adjust(item: FallbackTrailerYardBudget, value: Double): Double
}

data class RoundingFallbackTrailerYardBudgetAdjuster(var step: Double) : FallbackTrailerYardBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTrailerYardBudget, value: Double): Double = roundTo(value, step)
}

data class FallbackTrailerYardBudgetOdometerOffsetAdjuster(
    var weight: Double,
) : FallbackTrailerYardBudgetFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: FallbackTrailerYardBudget, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class NameFallbackTrailerYardBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackTrailerYardBudgetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackTrailerYardBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackTrailerYardBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackTrailerYardBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackTrailerYardBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback trailer yard budget steps in order and keeps a trace of each. */
data class FallbackTrailerYardBudgetEngine(
    var steps: MutableList<FallbackTrailerYardBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackTrailerYardBudget): Double {
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

    fun evaluateAll(items: MutableList<FallbackTrailerYardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackTrailerYardBudgetEngine(flags: Registry): FallbackTrailerYardBudgetEngine {
    return FallbackTrailerYardBudgetEngine(
        steps = mutableListOf<FallbackTrailerYardBudgetFactor>(
            RoundingFallbackTrailerYardBudgetAdjuster(0.05),
            FallbackTrailerYardBudgetOdometerOffsetAdjuster(1.38),
            NameFallbackTrailerYardBudgetAdjuster(mutableMapOf<String, Double>("east" to 0.88, "south" to 1.27)),
            FlaggedFallbackTrailerYardBudgetAdjuster(flags, "fallback-trailer-yard-budget-regional", -3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 78368.4,
    )
}
