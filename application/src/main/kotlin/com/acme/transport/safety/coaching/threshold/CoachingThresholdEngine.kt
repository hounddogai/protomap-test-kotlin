package com.acme.transport.safety.coaching.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching threshold calculation. */
interface CoachingThresholdStep {
    fun describe(): String
    fun adjust(item: CoachingThreshold, value: Double): Double
}

data class MinimumCoachingThresholdAdjuster(var minimum: Double) : CoachingThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameCoachingThresholdAdjuster(var factors: MutableMap<String, Double>) : CoachingThresholdStep {
    override fun describe(): String = "by name"

    override fun adjust(item: CoachingThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCoachingThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CoachingThresholdCreatedAtOffsetAdjuster(var weight: Double) : CoachingThresholdStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: CoachingThreshold, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class RoundingCoachingThresholdAdjuster(var step: Double) : CoachingThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the coaching threshold steps in order and keeps a trace of each. */
data class CoachingThresholdEngine(
    var steps: MutableList<CoachingThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CoachingThreshold): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCoachingThresholdEngine(): CoachingThresholdEngine {
    return CoachingThresholdEngine(
        steps = mutableListOf<CoachingThresholdStep>(
            MinimumCoachingThresholdAdjuster(37.9),
            NameCoachingThresholdAdjuster(mutableMapOf<String, Double>("north" to 0.87)),
            TieredCoachingThresholdAdjuster(
                mutableListOf<Double>(4628.0, 4990.0, 9874.0),
                mutableListOf<Double>(1.18, 1.14, 1.04),
            ),
            CoachingThresholdCreatedAtOffsetAdjuster(1.52),
            RoundingCoachingThresholdAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 57568.0,
    )
}
