package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient attempt plan calculation. */
interface LenientAttemptPlanStep {
    fun describe(): String
    fun adjust(item: LenientAttemptPlan, value: Double): Double
}

data class RoundingLenientAttemptPlanAdjuster(var step: Double) : LenientAttemptPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAttemptPlan, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientAttemptPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAttemptPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAttemptPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientAttemptPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAttemptPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAttemptPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientAttemptPlanAdjuster(var percent: Double) : LenientAttemptPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAttemptPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitLenientAttemptPlanAdjuster(var factors: MutableMap<String, Double>) : LenientAttemptPlanStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: LenientAttemptPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient attempt plan steps in order and keeps a trace of each. */
data class LenientAttemptPlanEngine(
    var steps: MutableList<LenientAttemptPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAttemptPlan): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientAttemptPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientAttemptPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAttemptPlanEngine(flags: Registry): LenientAttemptPlanEngine {
    return LenientAttemptPlanEngine(
        steps = mutableListOf<LenientAttemptPlanStep>(
            RoundingLenientAttemptPlanAdjuster(0.25),
            FlaggedLenientAttemptPlanAdjuster(flags, "lenient-attempt-plan-rolling", -4.5),
            TieredLenientAttemptPlanAdjuster(
                mutableListOf<Double>(243.0, 9670.0, 9744.0),
                mutableListOf<Double>(1.1, 0.81, 1.18),
            ),
            PercentLenientAttemptPlanAdjuster(-15.8),
            UnitLenientAttemptPlanAdjuster(mutableMapOf<String, Double>("east" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 68373.7,
    )
}
