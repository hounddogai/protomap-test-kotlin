package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered transit time budget calculation. */
interface TieredTransitTimeBudgetStep {
    fun describe(): String
    fun adjust(item: TieredTransitTimeBudget, value: Double): Double
}

data class RoundingTieredTransitTimeBudgetAdjuster(var step: Double) : TieredTransitTimeBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTransitTimeBudget, value: Double): Double = roundTo(value, step)
}

data class RegionTieredTransitTimeBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTransitTimeBudgetStep {
    override fun describe(): String = "by region"

    override fun adjust(item: TieredTransitTimeBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredTransitTimeBudgetAdjuster(var minimum: Double) : TieredTransitTimeBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTransitTimeBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered transit time budget steps in order and keeps a trace of each. */
data class TieredTransitTimeBudgetEngine(
    var steps: MutableList<TieredTransitTimeBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredTransitTimeBudget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredTransitTimeBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredTransitTimeBudgetEngine(): TieredTransitTimeBudgetEngine {
    return TieredTransitTimeBudgetEngine(
        steps = mutableListOf<TieredTransitTimeBudgetStep>(
            RoundingTieredTransitTimeBudgetAdjuster(5.0),
            RegionTieredTransitTimeBudgetAdjuster(mutableMapOf<String, Double>("north" to 1.19)),
            MinimumTieredTransitTimeBudgetAdjuster(25.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 78590.0,
    )
}
