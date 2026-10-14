package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global camera event budget calculation. */
interface GlobalCameraEventBudgetAdjuster {
    fun describe(): String
    fun adjust(item: GlobalCameraEventBudget, value: Double): Double
}

data class TieredGlobalCameraEventBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCameraEventBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCameraEventBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalCameraEventBudgetAdjuster(var minimum: Double) : GlobalCameraEventBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCameraEventBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalCameraEventBudgetAdjuster(var percent: Double) : GlobalCameraEventBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCameraEventBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalCameraEventBudgetLimitOffsetAdjuster(var weight: Double) : GlobalCameraEventBudgetAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: GlobalCameraEventBudget, value: Double): Double = value + item.limit.toDouble() * weight
}

data class CapGlobalCameraEventBudgetAdjuster(var cap: Double) : GlobalCameraEventBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCameraEventBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the global camera event budget steps in order and keeps a trace of each. */
data class GlobalCameraEventBudgetEngine(
    var steps: MutableList<GlobalCameraEventBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalCameraEventBudget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCameraEventBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalCameraEventBudgetEngine(): GlobalCameraEventBudgetEngine {
    return GlobalCameraEventBudgetEngine(
        steps = mutableListOf<GlobalCameraEventBudgetAdjuster>(
            TieredGlobalCameraEventBudgetAdjuster(
                mutableListOf<Double>(3419.0, 9231.0),
                mutableListOf<Double>(0.86, 1.09),
            ),
            MinimumGlobalCameraEventBudgetAdjuster(6.1),
            PercentGlobalCameraEventBudgetAdjuster(-18.4),
            GlobalCameraEventBudgetLimitOffsetAdjuster(1.3),
            CapGlobalCameraEventBudgetAdjuster(47921.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 60412.9,
    )
}
