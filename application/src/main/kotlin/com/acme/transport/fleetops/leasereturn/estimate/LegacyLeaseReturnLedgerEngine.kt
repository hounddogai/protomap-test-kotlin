package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the legacy lease return ledger calculation. */
interface LegacyLeaseReturnLedgerFactor {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double
}

data class CapLegacyLeaseReturnLedgerAdjuster(var cap: Double) : LegacyLeaseReturnLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyLeaseReturnLedgerAdjuster(var percent: Double) : LegacyLeaseReturnLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyLeaseReturnLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLeaseReturnLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyLeaseReturnLedgerAdjuster(var minimum: Double) : LegacyLeaseReturnLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotLegacyLeaseReturnLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyLeaseReturnLedgerFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyLeaseReturnLedgerPriorityOffsetAdjuster(var weight: Double) : LegacyLeaseReturnLedgerFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LegacyLeaseReturnLedger, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the legacy lease return ledger steps in order and keeps a trace of each. */
data class LegacyLeaseReturnLedgerEngine(
    var steps: MutableList<LegacyLeaseReturnLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyLeaseReturnLedger): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLeaseReturnLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLeaseReturnLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnLedgerEngine(flags: Registry): LegacyLeaseReturnLedgerEngine {
    return LegacyLeaseReturnLedgerEngine(
        steps = mutableListOf<LegacyLeaseReturnLedgerFactor>(
            CapLegacyLeaseReturnLedgerAdjuster(11539.0),
            PercentLegacyLeaseReturnLedgerAdjuster(-0.8),
            FlaggedLegacyLeaseReturnLedgerAdjuster(flags, "legacy-lease-return-ledger-adaptive", 5.4),
            MinimumLegacyLeaseReturnLedgerAdjuster(48.6),
            DepotLegacyLeaseReturnLedgerAdjuster(
                mutableMapOf<String, Double>("north" to 0.99, "west" to 1.01, "central" to 1.04),
            ),
            LegacyLeaseReturnLedgerPriorityOffsetAdjuster(1.07),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 86176.7,
    )
}
