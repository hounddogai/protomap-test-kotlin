package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node schedule calculation. */
interface NodeScheduleFactor {
    fun describe(): String
    fun adjust(item: NodeSchedule, value: Double): Double
}

data class CapNodeScheduleAdjuster(var cap: Double) : NodeScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentNodeScheduleAdjuster(var percent: Double) : NodeScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingNodeScheduleAdjuster(var step: Double) : NodeScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumNodeScheduleAdjuster(var minimum: Double) : NodeScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedNodeScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NodeScheduleRateOffsetAdjuster(var weight: Double) : NodeScheduleFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: NodeSchedule, value: Double): Double = value + item.rate * weight
}

/** Runs the node schedule steps in order and keeps a trace of each. */
data class NodeScheduleEngine(
    var steps: MutableList<NodeScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NodeSchedule): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<NodeSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNodeScheduleEngine(flags: Registry): NodeScheduleEngine {
    return NodeScheduleEngine(
        steps = mutableListOf<NodeScheduleFactor>(
            CapNodeScheduleAdjuster(3415.0),
            PercentNodeScheduleAdjuster(11.3),
            RoundingNodeScheduleAdjuster(0.25),
            MinimumNodeScheduleAdjuster(37.5),
            FlaggedNodeScheduleAdjuster(flags, "node-schedule-weighted", -6.5),
            NodeScheduleRateOffsetAdjuster(0.48),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 88911.8,
    )
}
