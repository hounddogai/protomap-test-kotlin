package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.kit.flags.Registry

/** One step of the ev charge ledger calculation. */
interface EvChargeLedgerFactor {
    fun describe(): String
    fun adjust(item: EvChargeLedger, value: Double): Double
}

data class TieredEvChargeLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentEvChargeLedgerAdjuster(var percent: Double) : EvChargeLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedEvChargeLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge ledger steps in order and keeps a trace of each. */
data class EvChargeLedgerEngine(
    var steps: MutableList<EvChargeLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeLedger): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeLedgerEngine(flags: Registry): EvChargeLedgerEngine {
    return EvChargeLedgerEngine(
        steps = mutableListOf<EvChargeLedgerFactor>(
            TieredEvChargeLedgerAdjuster(
                mutableListOf<Double>(652.0, 8095.0, 9164.0),
                mutableListOf<Double>(0.99, 1.13, 1.15),
            ),
            PercentEvChargeLedgerAdjuster(-16.0),
            FlaggedEvChargeLedgerAdjuster(flags, "ev-charge-ledger-fallback", -3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 27570.4,
    )
}
