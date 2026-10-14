package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt rollup calculation. */
interface AttemptRollupStep {
    fun describe(): String
    fun adjust(item: AttemptRollup, value: Double): Double
}

data class AttemptRollupCostOffsetAdjuster(var weight: Double) : AttemptRollupStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: AttemptRollup, value: Double): Double = value + item.cost * weight
}

data class RoundingAttemptRollupAdjuster(var step: Double) : AttemptRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptRollup, value: Double): Double = roundTo(value, step)
}

data class CapAttemptRollupAdjuster(var cap: Double) : AttemptRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptRollup, value: Double): Double = minOf(value, cap)
}

data class CategoryAttemptRollupAdjuster(var factors: MutableMap<String, Double>) : AttemptRollupStep {
    override fun describe(): String = "by category"

    override fun adjust(item: AttemptRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAttemptRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAttemptRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the attempt rollup steps in order and keeps a trace of each. */
data class AttemptRollupEngine(
    var steps: MutableList<AttemptRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AttemptRollup): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptRollupStep = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAttemptRollupEngine(flags: Registry): AttemptRollupEngine {
    return AttemptRollupEngine(
        steps = mutableListOf<AttemptRollupStep>(
            AttemptRollupCostOffsetAdjuster(1.47),
            RoundingAttemptRollupAdjuster(0.25),
            CapAttemptRollupAdjuster(36404.0),
            CategoryAttemptRollupAdjuster(mutableMapOf<String, Double>("retail" to 1.38, "south" to 0.89)),
            FlaggedAttemptRollupAdjuster(flags, "attempt-rollup-tiered", -5.0),
            TieredAttemptRollupAdjuster(
                mutableListOf<Double>(1250.0, 3411.0, 4633.0),
                mutableListOf<Double>(0.91, 1.08, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 27670.4,
    )
}
