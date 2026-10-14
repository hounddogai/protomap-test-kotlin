package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return ledger calculation. */
interface LeaseReturnLedgerAdjuster {
    fun describe(): String
    fun adjust(item: LeaseReturnLedger, value: Double): Double
}

data class PercentLeaseReturnLedgerAdjuster(var percent: Double) : LeaseReturnLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLeaseReturnLedgerAdjuster(var step: Double) : LeaseReturnLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnLedger, value: Double): Double = roundTo(value, step)
}

data class LeaseReturnLedgerStartsAtOffsetAdjuster(var weight: Double) : LeaseReturnLedgerAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: LeaseReturnLedger, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the lease return ledger steps in order and keeps a trace of each. */
data class LeaseReturnLedgerEngine(
    var steps: MutableList<LeaseReturnLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnLedger): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnLedgerEngine(): LeaseReturnLedgerEngine {
    return LeaseReturnLedgerEngine(
        steps = mutableListOf<LeaseReturnLedgerAdjuster>(
            PercentLeaseReturnLedgerAdjuster(-0.4),
            RoundingLeaseReturnLedgerAdjuster(0.05),
            LeaseReturnLedgerStartsAtOffsetAdjuster(1.66),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 86671.8,
    )
}
