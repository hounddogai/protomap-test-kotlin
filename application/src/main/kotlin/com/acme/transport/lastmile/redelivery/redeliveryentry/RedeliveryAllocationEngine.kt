package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery allocation calculation. */
interface RedeliveryAllocationStep {
    fun describe(): String
    fun adjust(item: RedeliveryAllocation, value: Double): Double
}

data class FlaggedRedeliveryAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelRedeliveryAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryAllocationStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: RedeliveryAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRedeliveryAllocationAdjuster(var step: Double) : RedeliveryAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumRedeliveryAllocationAdjuster(var minimum: Double) : RedeliveryAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery allocation steps in order and keeps a trace of each. */
data class RedeliveryAllocationEngine(
    var steps: MutableList<RedeliveryAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryAllocation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryAllocationEngine(flags: Registry): RedeliveryAllocationEngine {
    return RedeliveryAllocationEngine(
        steps = mutableListOf<RedeliveryAllocationStep>(
            FlaggedRedeliveryAllocationAdjuster(flags, "redelivery-allocation-legacy", -5.6),
            ChannelRedeliveryAllocationAdjuster(mutableMapOf<String, Double>("online" to 0.77, "north" to 1.21)),
            RoundingRedeliveryAllocationAdjuster(5.0),
            MinimumRedeliveryAllocationAdjuster(39.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 72898.0,
    )
}
