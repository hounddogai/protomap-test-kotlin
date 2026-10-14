package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return record calculation. */
interface LeaseReturnRecordAdjuster {
    fun describe(): String
    fun adjust(item: LeaseReturnRecord, value: Double): Double
}

data class PercentLeaseReturnRecordAdjuster(var percent: Double) : LeaseReturnRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LeaseReturnRecordBudgetOffsetAdjuster(var weight: Double) : LeaseReturnRecordAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: LeaseReturnRecord, value: Double): Double = value + item.budget * weight
}

data class RoundingLeaseReturnRecordAdjuster(var step: Double) : LeaseReturnRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return record steps in order and keeps a trace of each. */
data class LeaseReturnRecordEngine(
    var steps: MutableList<LeaseReturnRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnRecord): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnRecordEngine(): LeaseReturnRecordEngine {
    return LeaseReturnRecordEngine(
        steps = mutableListOf<LeaseReturnRecordAdjuster>(
            PercentLeaseReturnRecordAdjuster(-13.0),
            LeaseReturnRecordBudgetOffsetAdjuster(1.41),
            RoundingLeaseReturnRecordAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 5179.7,
    )
}
