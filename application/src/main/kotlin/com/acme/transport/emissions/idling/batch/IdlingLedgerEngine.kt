package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.flags.Registry

/** One step of the idling ledger calculation. */
interface IdlingLedgerFactor {
    fun describe(): String
    fun adjust(item: IdlingLedger, value: Double): Double
}

data class PercentIdlingLedgerAdjuster(var percent: Double) : IdlingLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredIdlingLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedIdlingLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumIdlingLedgerAdjuster(var minimum: Double) : IdlingLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the idling ledger steps in order and keeps a trace of each. */
data class IdlingLedgerEngine(
    var steps: MutableList<IdlingLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingLedger): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingLedgerEngine(flags: Registry): IdlingLedgerEngine {
    return IdlingLedgerEngine(
        steps = mutableListOf<IdlingLedgerFactor>(
            PercentIdlingLedgerAdjuster(20.8),
            TieredIdlingLedgerAdjuster(mutableListOf<Double>(4377.0, 7433.0), mutableListOf<Double>(0.88, 0.82)),
            FlaggedIdlingLedgerAdjuster(flags, "idling-ledger-rolling", -3.1),
            MinimumIdlingLedgerAdjuster(45.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 56859.2,
    )
}
