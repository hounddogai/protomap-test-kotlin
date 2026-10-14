package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled coaching override calculation. */
interface ScheduledCoachingOverrideStep {
    fun describe(): String
    fun adjust(item: ScheduledCoachingOverride, value: Double): Double
}

data class TieredScheduledCoachingOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCoachingOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCoachingOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledCoachingOverrideAdjuster(var step: Double) : ScheduledCoachingOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledCoachingOverride, value: Double): Double = roundTo(value, step)
}

data class CapScheduledCoachingOverrideAdjuster(var cap: Double) : ScheduledCoachingOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledCoachingOverride, value: Double): Double = minOf(value, cap)
}

data class NameScheduledCoachingOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledCoachingOverrideStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledCoachingOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScheduledCoachingOverrideAdjuster(var percent: Double) : ScheduledCoachingOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledCoachingOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled coaching override steps in order and keeps a trace of each. */
data class ScheduledCoachingOverrideEngine(
    var steps: MutableList<ScheduledCoachingOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledCoachingOverride): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledCoachingOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledCoachingOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledCoachingOverrideEngine(): ScheduledCoachingOverrideEngine {
    return ScheduledCoachingOverrideEngine(
        steps = mutableListOf<ScheduledCoachingOverrideStep>(
            TieredScheduledCoachingOverrideAdjuster(
                mutableListOf<Double>(3796.0, 4188.0, 5814.0, 7062.0),
                mutableListOf<Double>(0.84, 1.14, 0.89, 0.93),
            ),
            RoundingScheduledCoachingOverrideAdjuster(5.0),
            CapScheduledCoachingOverrideAdjuster(36452.0),
            NameScheduledCoachingOverrideAdjuster(
                mutableMapOf<String, Double>("online" to 0.81, "central" to 0.79, "west" to 1.28),
            ),
            PercentScheduledCoachingOverrideAdjuster(-3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 80126.6,
    )
}
