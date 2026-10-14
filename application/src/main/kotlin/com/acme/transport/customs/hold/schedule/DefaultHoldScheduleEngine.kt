package com.acme.transport.customs.hold.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the default hold schedule calculation. */
interface DefaultHoldScheduleStep {
    fun describe(): String
    fun adjust(item: DefaultHoldSchedule, value: Double): Double
}

data class RoundingDefaultHoldScheduleAdjuster(var step: Double) : DefaultHoldScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultHoldSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultHoldScheduleAdjuster(var minimum: Double) : DefaultHoldScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultHoldSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultHoldScheduleAdjuster(var cap: Double) : DefaultHoldScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultHoldSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultHoldScheduleAdjuster(var percent: Double) : DefaultHoldScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultHoldSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefaultHoldScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultHoldScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultHoldSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionDefaultHoldScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultHoldScheduleStep {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultHoldSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default hold schedule steps in order and keeps a trace of each. */
data class DefaultHoldScheduleEngine(
    var steps: MutableList<DefaultHoldScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultHoldSchedule): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultHoldScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultHoldSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultHoldScheduleEngine(): DefaultHoldScheduleEngine {
    return DefaultHoldScheduleEngine(
        steps = mutableListOf<DefaultHoldScheduleStep>(
            RoundingDefaultHoldScheduleAdjuster(0.25),
            MinimumDefaultHoldScheduleAdjuster(30.2),
            CapDefaultHoldScheduleAdjuster(49968.0),
            PercentDefaultHoldScheduleAdjuster(6.2),
            TieredDefaultHoldScheduleAdjuster(
                mutableListOf<Double>(135.0, 1019.0, 5204.0, 8701.0),
                mutableListOf<Double>(1.04, 0.81, 1.19, 0.99),
            ),
            RegionDefaultHoldScheduleAdjuster(mutableMapOf<String, Double>("east" to 1.37, "retail" to 0.74)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 19203.0,
    )
}
