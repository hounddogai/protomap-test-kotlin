package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled lease return ledger calculation. */
interface ScheduledLeaseReturnLedgerStep {
    fun describe(): String
    fun adjust(item: ScheduledLeaseReturnLedger, value: Double): Double
}

data class RoundingScheduledLeaseReturnLedgerAdjuster(var step: Double) : ScheduledLeaseReturnLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledLeaseReturnLedger, value: Double): Double = roundTo(value, step)
}

data class ScheduledLeaseReturnLedgerAmountOffsetAdjuster(var weight: Double) : ScheduledLeaseReturnLedgerStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ScheduledLeaseReturnLedger, value: Double): Double = value + item.amount * weight
}

data class CapScheduledLeaseReturnLedgerAdjuster(var cap: Double) : ScheduledLeaseReturnLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledLeaseReturnLedger, value: Double): Double = minOf(value, cap)
}

data class UnitScheduledLeaseReturnLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLeaseReturnLedgerStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledLeaseReturnLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledLeaseReturnLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLeaseReturnLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLeaseReturnLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled lease return ledger steps in order and keeps a trace of each. */
data class ScheduledLeaseReturnLedgerEngine(
    var steps: MutableList<ScheduledLeaseReturnLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledLeaseReturnLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLeaseReturnLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLeaseReturnLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledLeaseReturnLedgerEngine(): ScheduledLeaseReturnLedgerEngine {
    return ScheduledLeaseReturnLedgerEngine(
        steps = mutableListOf<ScheduledLeaseReturnLedgerStep>(
            RoundingScheduledLeaseReturnLedgerAdjuster(1.0),
            ScheduledLeaseReturnLedgerAmountOffsetAdjuster(1.04),
            CapScheduledLeaseReturnLedgerAdjuster(902.0),
            UnitScheduledLeaseReturnLedgerAdjuster(
                mutableMapOf<String, Double>("south" to 0.79, "retail" to 1.29, "central" to 1.21),
            ),
            TieredScheduledLeaseReturnLedgerAdjuster(
                mutableListOf<Double>(1366.0, 3822.0, 8518.0, 9688.0),
                mutableListOf<Double>(0.99, 1.01, 1.13, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 12648.1,
    )
}
