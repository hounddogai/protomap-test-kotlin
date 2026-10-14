package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending redelivery allocation calculation. */
interface PendingRedeliveryAllocationAdjuster {
    fun describe(): String
    fun adjust(item: PendingRedeliveryAllocation, value: Double): Double
}

data class PendingRedeliveryAllocationQuantityOffsetAdjuster(
    var weight: Double,
) : PendingRedeliveryAllocationAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: PendingRedeliveryAllocation, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class CapPendingRedeliveryAllocationAdjuster(var cap: Double) : PendingRedeliveryAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRedeliveryAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingRedeliveryAllocationAdjuster(var step: Double) : PendingRedeliveryAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRedeliveryAllocation, value: Double): Double = roundTo(value, step)
}

data class PercentPendingRedeliveryAllocationAdjuster(
    var percent: Double,
) : PendingRedeliveryAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRedeliveryAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending redelivery allocation steps in order and keeps a trace of each. */
data class PendingRedeliveryAllocationEngine(
    var steps: MutableList<PendingRedeliveryAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingRedeliveryAllocation): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingRedeliveryAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingRedeliveryAllocationEngine(): PendingRedeliveryAllocationEngine {
    return PendingRedeliveryAllocationEngine(
        steps = mutableListOf<PendingRedeliveryAllocationAdjuster>(
            PendingRedeliveryAllocationQuantityOffsetAdjuster(1.5),
            CapPendingRedeliveryAllocationAdjuster(15434.0),
            RoundingPendingRedeliveryAllocationAdjuster(5.0),
            PercentPendingRedeliveryAllocationAdjuster(-23.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 76649.2,
    )
}
