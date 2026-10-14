package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return request calculation. */
interface LeaseReturnRequestStep {
    fun describe(): String
    fun adjust(item: LeaseReturnRequest, value: Double): Double
}

data class RoundingLeaseReturnRequestAdjuster(var step: Double) : LeaseReturnRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnRequest, value: Double): Double = roundTo(value, step)
}

data class MinimumLeaseReturnRequestAdjuster(var minimum: Double) : LeaseReturnRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LeaseReturnRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLeaseReturnRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lease return request steps in order and keeps a trace of each. */
data class LeaseReturnRequestEngine(
    var steps: MutableList<LeaseReturnRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnRequest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnRequestEngine(): LeaseReturnRequestEngine {
    return LeaseReturnRequestEngine(
        steps = mutableListOf<LeaseReturnRequestStep>(
            RoundingLeaseReturnRequestAdjuster(0.1),
            MinimumLeaseReturnRequestAdjuster(18.1),
            TieredLeaseReturnRequestAdjuster(
                mutableListOf<Double>(184.0, 3781.0),
                mutableListOf<Double>(1.04, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 24226.8,
    )
}
