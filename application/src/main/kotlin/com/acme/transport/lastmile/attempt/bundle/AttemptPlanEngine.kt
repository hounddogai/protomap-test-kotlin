package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt plan calculation. */
interface AttemptPlanAdjuster {
    fun describe(): String
    fun adjust(item: AttemptPlan, value: Double): Double
}

data class TieredAttemptPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AttemptPlanMaximumOffsetAdjuster(var weight: Double) : AttemptPlanAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: AttemptPlan, value: Double): Double = value + item.maximum * weight
}

data class MinimumAttemptPlanAdjuster(var minimum: Double) : AttemptPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the attempt plan steps in order and keeps a trace of each. */
data class AttemptPlanEngine(
    var steps: MutableList<AttemptPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptPlan): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptPlanEngine(): AttemptPlanEngine {
    return AttemptPlanEngine(
        steps = mutableListOf<AttemptPlanAdjuster>(
            TieredAttemptPlanAdjuster(
                mutableListOf<Double>(82.0, 92.0, 3144.0),
                mutableListOf<Double>(1.11, 0.92, 0.89),
            ),
            AttemptPlanMaximumOffsetAdjuster(1.7),
            MinimumAttemptPlanAdjuster(13.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 93360.9,
    )
}
