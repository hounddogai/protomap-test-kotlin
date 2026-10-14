package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual roster plan calculation. */
interface ManualRosterPlanStep {
    fun describe(): String
    fun adjust(item: ManualRosterPlan, value: Double): Double
}

data class MinimumManualRosterPlanAdjuster(var minimum: Double) : ManualRosterPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRosterPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualRosterPlanWeightOffsetAdjuster(var weight: Double) : ManualRosterPlanStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: ManualRosterPlan, value: Double): Double = value + item.weight * weight
}

data class PercentManualRosterPlanAdjuster(var percent: Double) : ManualRosterPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRosterPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredManualRosterPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRosterPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRosterPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual roster plan steps in order and keeps a trace of each. */
data class ManualRosterPlanEngine(
    var steps: MutableList<ManualRosterPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualRosterPlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRosterPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualRosterPlanEngine(): ManualRosterPlanEngine {
    return ManualRosterPlanEngine(
        steps = mutableListOf<ManualRosterPlanStep>(
            MinimumManualRosterPlanAdjuster(16.0),
            ManualRosterPlanWeightOffsetAdjuster(0.63),
            PercentManualRosterPlanAdjuster(-3.6),
            TieredManualRosterPlanAdjuster(
                mutableListOf<Double>(3140.0, 9573.0),
                mutableListOf<Double>(1.13, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 31906.3,
    )
}
