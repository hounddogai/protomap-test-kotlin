package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback coaching budget calculation. */
interface FallbackCoachingBudgetFactor {
    fun describe(): String
    fun adjust(item: FallbackCoachingBudget, value: Double): Double
}

data class MinimumFallbackCoachingBudgetAdjuster(var minimum: Double) : FallbackCoachingBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCoachingBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackCoachingBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCoachingBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCoachingBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackCoachingBudgetPalletsOffsetAdjuster(var weight: Double) : FallbackCoachingBudgetFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: FallbackCoachingBudget, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentFallbackCoachingBudgetAdjuster(var percent: Double) : FallbackCoachingBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCoachingBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelFallbackCoachingBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCoachingBudgetFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackCoachingBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackCoachingBudgetAdjuster(var step: Double) : FallbackCoachingBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCoachingBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback coaching budget steps in order and keeps a trace of each. */
data class FallbackCoachingBudgetEngine(
    var steps: MutableList<FallbackCoachingBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackCoachingBudget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCoachingBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackCoachingBudgetEngine(): FallbackCoachingBudgetEngine {
    return FallbackCoachingBudgetEngine(
        steps = mutableListOf<FallbackCoachingBudgetFactor>(
            MinimumFallbackCoachingBudgetAdjuster(28.4),
            TieredFallbackCoachingBudgetAdjuster(
                mutableListOf<Double>(2751.0, 5661.0),
                mutableListOf<Double>(1.07, 1.03),
            ),
            FallbackCoachingBudgetPalletsOffsetAdjuster(1.38),
            PercentFallbackCoachingBudgetAdjuster(-6.4),
            LabelFallbackCoachingBudgetAdjuster(mutableMapOf<String, Double>("south" to 1.09, "east" to 0.98)),
            RoundingFallbackCoachingBudgetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 5304.8,
    )
}
