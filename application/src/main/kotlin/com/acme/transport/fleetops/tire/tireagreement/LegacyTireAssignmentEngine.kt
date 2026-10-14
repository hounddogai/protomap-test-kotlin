package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy tire assignment calculation. */
interface LegacyTireAssignmentStep {
    fun describe(): String
    fun adjust(item: LegacyTireAssignment, value: Double): Double
}

data class TieredLegacyTireAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTireAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTireAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyTireAssignmentAdjuster(var minimum: Double) : LegacyTireAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTireAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyTireAssignmentAdjuster(var step: Double) : LegacyTireAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTireAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy tire assignment steps in order and keeps a trace of each. */
data class LegacyTireAssignmentEngine(
    var steps: MutableList<LegacyTireAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyTireAssignment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTireAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTireAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyTireAssignmentEngine(): LegacyTireAssignmentEngine {
    return LegacyTireAssignmentEngine(
        steps = mutableListOf<LegacyTireAssignmentStep>(
            TieredLegacyTireAssignmentAdjuster(
                mutableListOf<Double>(2991.0, 5721.0, 8100.0, 9608.0),
                mutableListOf<Double>(0.97, 0.99, 1.1, 0.96),
            ),
            MinimumLegacyTireAssignmentAdjuster(2.4),
            RoundingLegacyTireAssignmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 74422.0,
    )
}
