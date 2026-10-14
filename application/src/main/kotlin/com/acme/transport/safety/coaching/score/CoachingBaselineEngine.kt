package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching baseline calculation. */
interface CoachingBaselineModifier {
    fun describe(): String
    fun adjust(item: CoachingBaseline, value: Double): Double
}

data class PercentCoachingBaselineAdjuster(var percent: Double) : CoachingBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCoachingBaselineAdjuster(var minimum: Double) : CoachingBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCoachingBaselineAdjuster(var step: Double) : CoachingBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingBaseline, value: Double): Double = roundTo(value, step)
}

data class UnitCoachingBaselineAdjuster(var factors: MutableMap<String, Double>) : CoachingBaselineModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: CoachingBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the coaching baseline steps in order and keeps a trace of each. */
data class CoachingBaselineEngine(
    var steps: MutableList<CoachingBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingBaseline): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingBaselineEngine(): CoachingBaselineEngine {
    return CoachingBaselineEngine(
        steps = mutableListOf<CoachingBaselineModifier>(
            PercentCoachingBaselineAdjuster(8.8),
            MinimumCoachingBaselineAdjuster(24.7),
            RoundingCoachingBaselineAdjuster(5.0),
            UnitCoachingBaselineAdjuster(mutableMapOf<String, Double>("east" to 0.78)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 2925.9,
    )
}
