package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted broker plan calculation. */
interface WeightedBrokerPlanStep {
    fun describe(): String
    fun adjust(item: WeightedBrokerPlan, value: Double): Double
}

data class WeightedBrokerPlanStopCountOffsetAdjuster(var weight: Double) : WeightedBrokerPlanStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: WeightedBrokerPlan, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredWeightedBrokerPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedBrokerPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedBrokerPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameWeightedBrokerPlanAdjuster(var factors: MutableMap<String, Double>) : WeightedBrokerPlanStep {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedBrokerPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedBrokerPlanAdjuster(var step: Double) : WeightedBrokerPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedBrokerPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted broker plan steps in order and keeps a trace of each. */
data class WeightedBrokerPlanEngine(
    var steps: MutableList<WeightedBrokerPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedBrokerPlan): Double {
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

    fun evaluateAll(items: MutableList<WeightedBrokerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedBrokerPlanEngine(): WeightedBrokerPlanEngine {
    return WeightedBrokerPlanEngine(
        steps = mutableListOf<WeightedBrokerPlanStep>(
            WeightedBrokerPlanStopCountOffsetAdjuster(1.6),
            TieredWeightedBrokerPlanAdjuster(
                mutableListOf<Double>(3199.0, 5902.0),
                mutableListOf<Double>(0.91, 1.14),
            ),
            NameWeightedBrokerPlanAdjuster(
                mutableMapOf<String, Double>("central" to 0.83, "south" to 0.86, "north" to 0.95),
            ),
            RoundingWeightedBrokerPlanAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 28731.6,
    )
}
