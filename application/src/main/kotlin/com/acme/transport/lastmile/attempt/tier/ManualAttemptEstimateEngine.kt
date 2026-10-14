package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual attempt estimate calculation. */
interface ManualAttemptEstimateFactor {
    fun describe(): String
    fun adjust(item: ManualAttemptEstimate, value: Double): Double
}

data class RoundingManualAttemptEstimateAdjuster(var step: Double) : ManualAttemptEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAttemptEstimate, value: Double): Double = roundTo(value, step)
}

data class TieredManualAttemptEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAttemptEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAttemptEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceManualAttemptEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAttemptEstimateFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: ManualAttemptEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualAttemptEstimateAdjuster(var cap: Double) : ManualAttemptEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAttemptEstimate, value: Double): Double = minOf(value, cap)
}

data class PercentManualAttemptEstimateAdjuster(var percent: Double) : ManualAttemptEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAttemptEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualAttemptEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAttemptEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAttemptEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual attempt estimate steps in order and keeps a trace of each. */
data class ManualAttemptEstimateEngine(
    var steps: MutableList<ManualAttemptEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualAttemptEstimate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAttemptEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualAttemptEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualAttemptEstimateEngine(flags: Registry): ManualAttemptEstimateEngine {
    return ManualAttemptEstimateEngine(
        steps = mutableListOf<ManualAttemptEstimateFactor>(
            RoundingManualAttemptEstimateAdjuster(0.05),
            TieredManualAttemptEstimateAdjuster(
                mutableListOf<Double>(261.0, 8480.0, 9060.0, 9244.0),
                mutableListOf<Double>(0.96, 1.05, 1.07, 1.17),
            ),
            SourceManualAttemptEstimateAdjuster(mutableMapOf<String, Double>("east" to 0.72)),
            CapManualAttemptEstimateAdjuster(39327.0),
            PercentManualAttemptEstimateAdjuster(-5.3),
            FlaggedManualAttemptEstimateAdjuster(flags, "manual-attempt-estimate-capped", -9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 31880.3,
    )
}
