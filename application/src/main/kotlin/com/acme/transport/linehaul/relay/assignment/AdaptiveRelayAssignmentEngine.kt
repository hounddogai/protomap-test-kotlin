package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive relay assignment calculation. */
interface AdaptiveRelayAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveRelayAssignment, value: Double): Double
}

data class FlaggedAdaptiveRelayAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveRelayAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveRelayAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveRelayAssignmentAdjuster(var step: Double) : AdaptiveRelayAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRelayAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveRelayAssignmentAdjuster(var minimum: Double) : AdaptiveRelayAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRelayAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive relay assignment steps in order and keeps a trace of each. */
data class AdaptiveRelayAssignmentEngine(
    var steps: MutableList<AdaptiveRelayAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveRelayAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveRelayAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveRelayAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveRelayAssignmentEngine(flags: Registry): AdaptiveRelayAssignmentEngine {
    return AdaptiveRelayAssignmentEngine(
        steps = mutableListOf<AdaptiveRelayAssignmentAdjuster>(
            FlaggedAdaptiveRelayAssignmentAdjuster(flags, "adaptive-relay-assignment-fallback", -3.7),
            RoundingAdaptiveRelayAssignmentAdjuster(0.5),
            MinimumAdaptiveRelayAssignmentAdjuster(29.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 59646.3,
    )
}
