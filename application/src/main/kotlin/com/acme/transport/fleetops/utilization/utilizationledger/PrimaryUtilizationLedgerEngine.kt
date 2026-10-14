package com.acme.transport.fleetops.utilization.utilizationledger

/** One step of the primary utilization ledger calculation. */
interface PrimaryUtilizationLedgerFactor {
    fun describe(): String
    fun adjust(item: PrimaryUtilizationLedger, value: Double): Double
}

data class MinimumPrimaryUtilizationLedgerAdjuster(var minimum: Double) : PrimaryUtilizationLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryUtilizationLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryUtilizationLedgerAdjuster(var percent: Double) : PrimaryUtilizationLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryUtilizationLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryUtilizationLedgerAdjuster(var cap: Double) : PrimaryUtilizationLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryUtilizationLedger, value: Double): Double = minOf(value, cap)
}

data class PrimaryUtilizationLedgerEtaMinutesOffsetAdjuster(
    var weight: Double,
) : PrimaryUtilizationLedgerFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: PrimaryUtilizationLedger, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class ChannelPrimaryUtilizationLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryUtilizationLedgerFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: PrimaryUtilizationLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary utilization ledger steps in order and keeps a trace of each. */
data class PrimaryUtilizationLedgerEngine(
    var steps: MutableList<PrimaryUtilizationLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryUtilizationLedger): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryUtilizationLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryUtilizationLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryUtilizationLedgerEngine(): PrimaryUtilizationLedgerEngine {
    return PrimaryUtilizationLedgerEngine(
        steps = mutableListOf<PrimaryUtilizationLedgerFactor>(
            MinimumPrimaryUtilizationLedgerAdjuster(24.5),
            PercentPrimaryUtilizationLedgerAdjuster(13.2),
            CapPrimaryUtilizationLedgerAdjuster(10176.0),
            PrimaryUtilizationLedgerEtaMinutesOffsetAdjuster(0.74),
            ChannelPrimaryUtilizationLedgerAdjuster(
                mutableMapOf<String, Double>("west" to 0.95, "east" to 0.75, "central" to 1.24),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 79348.8,
    )
}
