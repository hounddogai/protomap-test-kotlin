package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled relay assignment calculation. */
interface ScheduledRelayAssignmentModifier {
    fun describe(): String
    fun adjust(item: ScheduledRelayAssignment, value: Double): Double
}

data class ScheduledRelayAssignmentExpiresAtOffsetAdjuster(
    var weight: Double,
) : ScheduledRelayAssignmentModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ScheduledRelayAssignment, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class RoundingScheduledRelayAssignmentAdjuster(var step: Double) : ScheduledRelayAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRelayAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledRelayAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRelayAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRelayAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneScheduledRelayAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRelayAssignmentModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledRelayAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled relay assignment steps in order and keeps a trace of each. */
data class ScheduledRelayAssignmentEngine(
    var steps: MutableList<ScheduledRelayAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledRelayAssignment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRelayAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRelayAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledRelayAssignmentEngine(): ScheduledRelayAssignmentEngine {
    return ScheduledRelayAssignmentEngine(
        steps = mutableListOf<ScheduledRelayAssignmentModifier>(
            ScheduledRelayAssignmentExpiresAtOffsetAdjuster(1.23),
            RoundingScheduledRelayAssignmentAdjuster(0.1),
            TieredScheduledRelayAssignmentAdjuster(
                mutableListOf<Double>(6217.0, 8617.0, 8877.0),
                mutableListOf<Double>(1.09, 1.16, 0.81),
            ),
            LaneScheduledRelayAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.3, "south" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 94268.0,
    )
}
