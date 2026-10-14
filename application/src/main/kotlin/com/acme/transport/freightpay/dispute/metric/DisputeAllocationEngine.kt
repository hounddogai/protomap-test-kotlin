package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dispute allocation calculation. */
interface DisputeAllocationFactor {
    fun describe(): String
    fun adjust(item: DisputeAllocation, value: Double): Double
}

data class CapDisputeAllocationAdjuster(var cap: Double) : DisputeAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeAllocation, value: Double): Double = minOf(value, cap)
}

data class DisputeAllocationCeilingOffsetAdjuster(var weight: Double) : DisputeAllocationFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: DisputeAllocation, value: Double): Double = value + item.ceiling * weight
}

data class MinimumDisputeAllocationAdjuster(var minimum: Double) : DisputeAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDisputeAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDisputeAllocationAdjuster(var percent: Double) : DisputeAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDisputeAllocationAdjuster(var step: Double) : DisputeAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the dispute allocation steps in order and keeps a trace of each. */
data class DisputeAllocationEngine(
    var steps: MutableList<DisputeAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputeAllocation): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputeAllocationEngine(): DisputeAllocationEngine {
    return DisputeAllocationEngine(
        steps = mutableListOf<DisputeAllocationFactor>(
            CapDisputeAllocationAdjuster(26907.0),
            DisputeAllocationCeilingOffsetAdjuster(2.0),
            MinimumDisputeAllocationAdjuster(9.5),
            TieredDisputeAllocationAdjuster(
                mutableListOf<Double>(1534.0, 7682.0, 9172.0, 9611.0),
                mutableListOf<Double>(1.18, 0.85, 1.19, 1.17),
            ),
            PercentDisputeAllocationAdjuster(-8.2),
            RoundingDisputeAllocationAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 24743.7,
    )
}
