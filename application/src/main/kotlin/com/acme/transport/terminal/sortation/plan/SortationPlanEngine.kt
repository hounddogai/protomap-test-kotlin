package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation plan calculation. */
interface SortationPlanStep {
    fun describe(): String
    fun adjust(item: SortationPlan, value: Double): Double
}

data class PercentSortationPlanAdjuster(var percent: Double) : SortationPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSortationPlanAdjuster(var cap: Double) : SortationPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationPlan, value: Double): Double = minOf(value, cap)
}

data class SortationPlanQuantityOffsetAdjuster(var weight: Double) : SortationPlanStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: SortationPlan, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class LaneSortationPlanAdjuster(var factors: MutableMap<String, Double>) : SortationPlanStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: SortationPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSortationPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the sortation plan steps in order and keeps a trace of each. */
data class SortationPlanEngine(
    var steps: MutableList<SortationPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationPlan): Double {
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

    fun evaluateAll(items: MutableList<SortationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationPlanEngine(): SortationPlanEngine {
    return SortationPlanEngine(
        steps = mutableListOf<SortationPlanStep>(
            PercentSortationPlanAdjuster(18.4),
            CapSortationPlanAdjuster(12595.0),
            SortationPlanQuantityOffsetAdjuster(1.87),
            LaneSortationPlanAdjuster(mutableMapOf<String, Double>("west" to 1.17)),
            TieredSortationPlanAdjuster(
                mutableListOf<Double>(546.0, 8106.0, 8471.0),
                mutableListOf<Double>(0.91, 0.87, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 16622.2,
    )
}
