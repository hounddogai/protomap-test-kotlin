package com.acme.transport.network.node.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node reservation calculation. */
interface NodeReservationStep {
    fun describe(): String
    fun adjust(item: NodeReservation, value: Double): Double
}

data class MinimumNodeReservationAdjuster(var minimum: Double) : NodeReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingNodeReservationAdjuster(var step: Double) : NodeReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeReservation, value: Double): Double = roundTo(value, step)
}

data class TieredNodeReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNodeReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentNodeReservationAdjuster(var percent: Double) : NodeReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the node reservation steps in order and keeps a trace of each. */
data class NodeReservationEngine(
    var steps: MutableList<NodeReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NodeReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<NodeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNodeReservationEngine(flags: Registry): NodeReservationEngine {
    return NodeReservationEngine(
        steps = mutableListOf<NodeReservationStep>(
            MinimumNodeReservationAdjuster(37.7),
            RoundingNodeReservationAdjuster(1.0),
            TieredNodeReservationAdjuster(
                mutableListOf<Double>(2141.0, 3647.0, 8235.0, 9504.0),
                mutableListOf<Double>(0.94, 0.85, 1.02, 0.86),
            ),
            FlaggedNodeReservationAdjuster(flags, "node-reservation-capped", -6.3),
            PercentNodeReservationAdjuster(-12.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 78462.8,
    )
}
