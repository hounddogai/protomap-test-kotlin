package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching mapping calculation. */
interface CoachingMappingStep {
    fun describe(): String
    fun adjust(item: CoachingMapping, value: Double): Double
}

data class RoundingCoachingMappingAdjuster(var step: Double) : CoachingMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumCoachingMappingAdjuster(var minimum: Double) : CoachingMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotCoachingMappingAdjuster(var factors: MutableMap<String, Double>) : CoachingMappingStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: CoachingMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCoachingMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the coaching mapping steps in order and keeps a trace of each. */
data class CoachingMappingEngine(
    var steps: MutableList<CoachingMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingMapping): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingMappingStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingMappingEngine(): CoachingMappingEngine {
    return CoachingMappingEngine(
        steps = mutableListOf<CoachingMappingStep>(
            RoundingCoachingMappingAdjuster(5.0),
            MinimumCoachingMappingAdjuster(13.3),
            DepotCoachingMappingAdjuster(mutableMapOf<String, Double>("central" to 0.75)),
            TieredCoachingMappingAdjuster(
                mutableListOf<Double>(697.0, 2929.0, 9592.0),
                mutableListOf<Double>(0.99, 1.03, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 31661.4,
    )
}
