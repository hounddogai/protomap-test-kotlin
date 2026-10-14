package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return adjustment calculation. */
interface LeaseReturnAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: LeaseReturnAdjustment, value: Double): Double
}

data class RoundingLeaseReturnAdjustmentAdjuster(var step: Double) : LeaseReturnAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentLeaseReturnAdjustmentAdjuster(var percent: Double) : LeaseReturnAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLeaseReturnAdjustmentAdjuster(var cap: Double) : LeaseReturnAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredLeaseReturnAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lease return adjustment steps in order and keeps a trace of each. */
data class LeaseReturnAdjustmentEngine(
    var steps: MutableList<LeaseReturnAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnAdjustment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnAdjustmentEngine(): LeaseReturnAdjustmentEngine {
    return LeaseReturnAdjustmentEngine(
        steps = mutableListOf<LeaseReturnAdjustmentAdjuster>(
            RoundingLeaseReturnAdjustmentAdjuster(1.0),
            PercentLeaseReturnAdjustmentAdjuster(-1.6),
            CapLeaseReturnAdjustmentAdjuster(23304.0),
            TieredLeaseReturnAdjustmentAdjuster(
                mutableListOf<Double>(2567.0, 5919.0, 8194.0, 9102.0),
                mutableListOf<Double>(1.03, 0.92, 1.08, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 48997.7,
    )
}
