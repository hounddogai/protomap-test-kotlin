package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending broker allocation calculation. */
interface PendingBrokerAllocationFactor {
    fun describe(): String
    fun adjust(item: PendingBrokerAllocation, value: Double): Double
}

data class FlaggedPendingBrokerAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingBrokerAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingBrokerAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingBrokerAllocationAdjuster(var step: Double) : PendingBrokerAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingBrokerAllocation, value: Double): Double = roundTo(value, step)
}

data class PendingBrokerAllocationDiscountOffsetAdjuster(var weight: Double) : PendingBrokerAllocationFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: PendingBrokerAllocation, value: Double): Double = value + item.discount * weight
}

/** Runs the pending broker allocation steps in order and keeps a trace of each. */
data class PendingBrokerAllocationEngine(
    var steps: MutableList<PendingBrokerAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingBrokerAllocation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingBrokerAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingBrokerAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingBrokerAllocationEngine(flags: Registry): PendingBrokerAllocationEngine {
    return PendingBrokerAllocationEngine(
        steps = mutableListOf<PendingBrokerAllocationFactor>(
            FlaggedPendingBrokerAllocationAdjuster(flags, "pending-broker-allocation-capped", -7.4),
            RoundingPendingBrokerAllocationAdjuster(0.1),
            PendingBrokerAllocationDiscountOffsetAdjuster(1.74),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 36477.3,
    )
}
