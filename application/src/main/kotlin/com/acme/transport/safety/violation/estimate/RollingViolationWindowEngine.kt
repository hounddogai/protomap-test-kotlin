package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling violation window calculation. */
interface RollingViolationWindowStep {
    fun describe(): String
    fun adjust(item: RollingViolationWindow, value: Double): Double
}

data class TieredRollingViolationWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingViolationWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingViolationWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingViolationWindowAdjuster(var step: Double) : RollingViolationWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingViolationWindow, value: Double): Double = roundTo(value, step)
}

data class RegionRollingViolationWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingViolationWindowStep {
    override fun describe(): String = "by region"

    override fun adjust(item: RollingViolationWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling violation window steps in order and keeps a trace of each. */
data class RollingViolationWindowEngine(
    var steps: MutableList<RollingViolationWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingViolationWindow): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingViolationWindowStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingViolationWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingViolationWindowEngine(): RollingViolationWindowEngine {
    return RollingViolationWindowEngine(
        steps = mutableListOf<RollingViolationWindowStep>(
            TieredRollingViolationWindowAdjuster(
                mutableListOf<Double>(4987.0, 7561.0, 9159.0),
                mutableListOf<Double>(1.06, 1.03, 0.92),
            ),
            RoundingRollingViolationWindowAdjuster(1.0),
            RegionRollingViolationWindowAdjuster(
                mutableMapOf<String, Double>("online" to 0.83, "east" to 0.72, "south" to 1.35),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 86562.7,
    )
}
