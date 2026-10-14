package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the parcel ledger calculation. */
interface ParcelLedgerFactor {
    fun describe(): String
    fun adjust(item: ParcelLedger, value: Double): Double
}

data class PercentParcelLedgerAdjuster(var percent: Double) : ParcelLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelLedgerAdjuster(var minimum: Double) : ParcelLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedParcelLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the parcel ledger steps in order and keeps a trace of each. */
data class ParcelLedgerEngine(
    var steps: MutableList<ParcelLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelLedger): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelLedgerEngine(flags: Registry): ParcelLedgerEngine {
    return ParcelLedgerEngine(
        steps = mutableListOf<ParcelLedgerFactor>(
            PercentParcelLedgerAdjuster(-18.9),
            MinimumParcelLedgerAdjuster(3.6),
            FlaggedParcelLedgerAdjuster(flags, "parcel-ledger-tiered", 1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 57776.4,
    )
}
