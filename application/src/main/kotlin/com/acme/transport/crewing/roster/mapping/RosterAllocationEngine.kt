package com.acme.transport.crewing.roster.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the roster allocation calculation. */
interface RosterAllocationStep {
    fun describe(): String
    fun adjust(item: RosterAllocation, value: Double): Double
}

data class FlaggedRosterAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRosterAllocationAdjuster(var cap: Double) : RosterAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterAllocation, value: Double): Double = minOf(value, cap)
}

data class PercentRosterAllocationAdjuster(var percent: Double) : RosterAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRosterAllocationAdjuster(var minimum: Double) : RosterAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RosterAllocationBatchSizeOffsetAdjuster(var weight: Double) : RosterAllocationStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RosterAllocation, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the roster allocation steps in order and keeps a trace of each. */
data class RosterAllocationEngine(
    var steps: MutableList<RosterAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RosterAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<RosterAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRosterAllocationEngine(flags: Registry): RosterAllocationEngine {
    return RosterAllocationEngine(
        steps = mutableListOf<RosterAllocationStep>(
            FlaggedRosterAllocationAdjuster(flags, "roster-allocation-lenient", -7.5),
            CapRosterAllocationAdjuster(2274.0),
            PercentRosterAllocationAdjuster(19.4),
            MinimumRosterAllocationAdjuster(27.2),
            RosterAllocationBatchSizeOffsetAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 48245.4,
    )
}
