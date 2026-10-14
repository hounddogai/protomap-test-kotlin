package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary scorecard adjustment calculation. */
interface PrimaryScorecardAdjustmentStep {
    fun describe(): String
    fun adjust(item: PrimaryScorecardAdjustment, value: Double): Double
}

data class PrimaryScorecardAdjustmentVersionOffsetAdjuster(
    var weight: Double,
) : PrimaryScorecardAdjustmentStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PrimaryScorecardAdjustment, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingPrimaryScorecardAdjustmentAdjuster(var step: Double) : PrimaryScorecardAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryScorecardAdjustment, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryScorecardAdjustmentAdjuster(var minimum: Double) : PrimaryScorecardAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryScorecardAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryScorecardAdjustmentAdjuster(var cap: Double) : PrimaryScorecardAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryScorecardAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the primary scorecard adjustment steps in order and keeps a trace of each. */
data class PrimaryScorecardAdjustmentEngine(
    var steps: MutableList<PrimaryScorecardAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryScorecardAdjustment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryScorecardAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryScorecardAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryScorecardAdjustmentEngine(): PrimaryScorecardAdjustmentEngine {
    return PrimaryScorecardAdjustmentEngine(
        steps = mutableListOf<PrimaryScorecardAdjustmentStep>(
            PrimaryScorecardAdjustmentVersionOffsetAdjuster(0.29),
            RoundingPrimaryScorecardAdjustmentAdjuster(1.0),
            MinimumPrimaryScorecardAdjustmentAdjuster(37.5),
            CapPrimaryScorecardAdjustmentAdjuster(39099.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 32500.2,
    )
}
