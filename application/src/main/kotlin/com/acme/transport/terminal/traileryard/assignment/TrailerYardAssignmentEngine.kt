package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard assignment calculation. */
interface TrailerYardAssignmentModifier {
    fun describe(): String
    fun adjust(item: TrailerYardAssignment, value: Double): Double
}

data class FlaggedTrailerYardAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerYardAssignmentThresholdOffsetAdjuster(var weight: Double) : TrailerYardAssignmentModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: TrailerYardAssignment, value: Double): Double = value + item.threshold * weight
}

data class RoundingTrailerYardAssignmentAdjuster(var step: Double) : TrailerYardAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumTrailerYardAssignmentAdjuster(var minimum: Double) : TrailerYardAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard assignment steps in order and keeps a trace of each. */
data class TrailerYardAssignmentEngine(
    var steps: MutableList<TrailerYardAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardAssignment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardAssignmentEngine(flags: Registry): TrailerYardAssignmentEngine {
    return TrailerYardAssignmentEngine(
        steps = mutableListOf<TrailerYardAssignmentModifier>(
            FlaggedTrailerYardAssignmentAdjuster(flags, "trailer-yard-assignment-adaptive", -4.5),
            TrailerYardAssignmentThresholdOffsetAdjuster(0.3),
            RoundingTrailerYardAssignmentAdjuster(1.0),
            MinimumTrailerYardAssignmentAdjuster(11.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 17222.4,
    )
}
