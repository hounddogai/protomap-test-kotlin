package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive violation cycle calculation. */
interface AdaptiveViolationCycleAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveViolationCycle, value: Double): Double
}

data class RoundingAdaptiveViolationCycleAdjuster(var step: Double) : AdaptiveViolationCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveViolationCycle, value: Double): Double = roundTo(value, step)
}

data class TitleAdaptiveViolationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveViolationCycleAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveViolationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveViolationCycleAdjuster(var minimum: Double) : AdaptiveViolationCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveViolationCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveViolationCycleAdjuster(var percent: Double) : AdaptiveViolationCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveViolationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveViolationCycleAttemptsOffsetAdjuster(var weight: Double) : AdaptiveViolationCycleAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AdaptiveViolationCycle, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the adaptive violation cycle steps in order and keeps a trace of each. */
data class AdaptiveViolationCycleEngine(
    var steps: MutableList<AdaptiveViolationCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveViolationCycle): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveViolationCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveViolationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveViolationCycleEngine(): AdaptiveViolationCycleEngine {
    return AdaptiveViolationCycleEngine(
        steps = mutableListOf<AdaptiveViolationCycleAdjuster>(
            RoundingAdaptiveViolationCycleAdjuster(0.1),
            TitleAdaptiveViolationCycleAdjuster(
                mutableMapOf<String, Double>("online" to 1.25, "south" to 1.34, "retail" to 0.93),
            ),
            MinimumAdaptiveViolationCycleAdjuster(42.7),
            PercentAdaptiveViolationCycleAdjuster(-20.8),
            AdaptiveViolationCycleAttemptsOffsetAdjuster(1.43),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 77966.4,
    )
}
