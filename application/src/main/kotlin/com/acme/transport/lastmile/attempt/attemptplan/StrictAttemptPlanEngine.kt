package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict attempt plan calculation. */
interface StrictAttemptPlanStep {
    fun describe(): String
    fun adjust(item: StrictAttemptPlan, value: Double): Double
}

data class PercentStrictAttemptPlanAdjuster(var percent: Double) : StrictAttemptPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictAttemptPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumStrictAttemptPlanAdjuster(var minimum: Double) : StrictAttemptPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAttemptPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictAttemptPlanAdjuster(var cap: Double) : StrictAttemptPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictAttemptPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictAttemptPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAttemptPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAttemptPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictAttemptPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictAttemptPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictAttemptPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict attempt plan steps in order and keeps a trace of each. */
data class StrictAttemptPlanEngine(
    var steps: MutableList<StrictAttemptPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictAttemptPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictAttemptPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictAttemptPlanEngine(flags: Registry): StrictAttemptPlanEngine {
    return StrictAttemptPlanEngine(
        steps = mutableListOf<StrictAttemptPlanStep>(
            PercentStrictAttemptPlanAdjuster(-14.9),
            MinimumStrictAttemptPlanAdjuster(1.7),
            CapStrictAttemptPlanAdjuster(29821.0),
            FlaggedStrictAttemptPlanAdjuster(flags, "strict-attempt-plan-lenient", -0.8),
            TieredStrictAttemptPlanAdjuster(
                mutableListOf<Double>(1747.0, 4573.0, 5384.0),
                mutableListOf<Double>(0.92, 0.97, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 55792.7,
    )
}
