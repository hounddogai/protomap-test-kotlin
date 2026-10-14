package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster checkpoint calculation. */
interface RosterCheckpointStep {
    fun describe(): String
    fun adjust(item: RosterCheckpoint, value: Double): Double
}

data class TieredRosterCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameRosterCheckpointAdjuster(var factors: MutableMap<String, Double>) : RosterCheckpointStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RosterCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RosterCheckpointCapacityOffsetAdjuster(var weight: Double) : RosterCheckpointStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RosterCheckpoint, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class RoundingRosterCheckpointAdjuster(var step: Double) : RosterCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapRosterCheckpointAdjuster(var cap: Double) : RosterCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the roster checkpoint steps in order and keeps a trace of each. */
data class RosterCheckpointEngine(
    var steps: MutableList<RosterCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<RosterCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterCheckpointEngine(): RosterCheckpointEngine {
    return RosterCheckpointEngine(
        steps = mutableListOf<RosterCheckpointStep>(
            TieredRosterCheckpointAdjuster(
                mutableListOf<Double>(3248.0, 7531.0),
                mutableListOf<Double>(1.19, 0.91),
            ),
            NameRosterCheckpointAdjuster(
                mutableMapOf<String, Double>("online" to 1.33, "north" to 1.01, "south" to 1.14),
            ),
            RosterCheckpointCapacityOffsetAdjuster(1.59),
            RoundingRosterCheckpointAdjuster(0.1),
            CapRosterCheckpointAdjuster(30869.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 34364.2,
    )
}
