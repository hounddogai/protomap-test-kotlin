package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay assignment calculation. */
interface RelayAssignmentModifier {
    fun describe(): String
    fun adjust(item: RelayAssignment, value: Double): Double
}

data class FlaggedRelayAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceRelayAssignmentAdjuster(var factors: MutableMap<String, Double>) : RelayAssignmentModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: RelayAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRelayAssignmentAdjuster(var step: Double) : RelayAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the relay assignment steps in order and keeps a trace of each. */
data class RelayAssignmentEngine(
    var steps: MutableList<RelayAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RelayAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayAssignmentEngine(flags: Registry): RelayAssignmentEngine {
    return RelayAssignmentEngine(
        steps = mutableListOf<RelayAssignmentModifier>(
            FlaggedRelayAssignmentAdjuster(flags, "relay-assignment-weighted", -2.9),
            SourceRelayAssignmentAdjuster(mutableMapOf<String, Double>("west" to 1.39)),
            RoundingRelayAssignmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 46157.6,
    )
}
