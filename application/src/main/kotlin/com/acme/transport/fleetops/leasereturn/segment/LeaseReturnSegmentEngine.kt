package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lease return segment calculation. */
interface LeaseReturnSegmentStep {
    fun describe(): String
    fun adjust(item: LeaseReturnSegment, value: Double): Double
}

data class LeaseReturnSegmentBudgetOffsetAdjuster(var weight: Double) : LeaseReturnSegmentStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: LeaseReturnSegment, value: Double): Double = value + item.budget * weight
}

data class CapLeaseReturnSegmentAdjuster(var cap: Double) : LeaseReturnSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumLeaseReturnSegmentAdjuster(var minimum: Double) : LeaseReturnSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLeaseReturnSegmentAdjuster(var percent: Double) : LeaseReturnSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lease return segment steps in order and keeps a trace of each. */
data class LeaseReturnSegmentEngine(
    var steps: MutableList<LeaseReturnSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnSegment): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnSegmentEngine(): LeaseReturnSegmentEngine {
    return LeaseReturnSegmentEngine(
        steps = mutableListOf<LeaseReturnSegmentStep>(
            LeaseReturnSegmentBudgetOffsetAdjuster(1.01),
            CapLeaseReturnSegmentAdjuster(505.0),
            MinimumLeaseReturnSegmentAdjuster(14.5),
            PercentLeaseReturnSegmentAdjuster(12.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 84341.6,
    )
}
