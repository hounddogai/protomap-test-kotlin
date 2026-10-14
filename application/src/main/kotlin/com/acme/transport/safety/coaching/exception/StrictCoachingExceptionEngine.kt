package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict coaching exception calculation. */
interface StrictCoachingExceptionStep {
    fun describe(): String
    fun adjust(item: StrictCoachingException, value: Double): Double
}

data class StrictCoachingExceptionFuelLitersOffsetAdjuster(var weight: Double) : StrictCoachingExceptionStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: StrictCoachingException, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredStrictCoachingExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCoachingExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCoachingException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedStrictCoachingExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCoachingExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCoachingException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictCoachingExceptionAdjuster(var step: Double) : StrictCoachingExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCoachingException, value: Double): Double = roundTo(value, step)
}

/** Runs the strict coaching exception steps in order and keeps a trace of each. */
data class StrictCoachingExceptionEngine(
    var steps: MutableList<StrictCoachingExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictCoachingException): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCoachingExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCoachingException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictCoachingExceptionEngine(flags: Registry): StrictCoachingExceptionEngine {
    return StrictCoachingExceptionEngine(
        steps = mutableListOf<StrictCoachingExceptionStep>(
            StrictCoachingExceptionFuelLitersOffsetAdjuster(1.89),
            TieredStrictCoachingExceptionAdjuster(
                mutableListOf<Double>(2303.0, 3995.0, 8423.0),
                mutableListOf<Double>(1.13, 1.08, 1.11),
            ),
            FlaggedStrictCoachingExceptionAdjuster(flags, "strict-coaching-exception-default", 5.0),
            RoundingStrictCoachingExceptionAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 48416.6,
    )
}
