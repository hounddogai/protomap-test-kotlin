package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global transit time ledger calculation. */
interface GlobalTransitTimeLedgerStep {
    fun describe(): String
    fun adjust(item: GlobalTransitTimeLedger, value: Double): Double
}

data class RoundingGlobalTransitTimeLedgerAdjuster(var step: Double) : GlobalTransitTimeLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTransitTimeLedger, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalTransitTimeLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTransitTimeLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTransitTimeLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalTransitTimeLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalTransitTimeLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalTransitTimeLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalTransitTimeLedgerExpiresAtOffsetAdjuster(var weight: Double) : GlobalTransitTimeLedgerStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: GlobalTransitTimeLedger, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the global transit time ledger steps in order and keeps a trace of each. */
data class GlobalTransitTimeLedgerEngine(
    var steps: MutableList<GlobalTransitTimeLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalTransitTimeLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalTransitTimeLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalTransitTimeLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalTransitTimeLedgerEngine(flags: Registry): GlobalTransitTimeLedgerEngine {
    return GlobalTransitTimeLedgerEngine(
        steps = mutableListOf<GlobalTransitTimeLedgerStep>(
            RoundingGlobalTransitTimeLedgerAdjuster(5.0),
            TieredGlobalTransitTimeLedgerAdjuster(
                mutableListOf<Double>(669.0, 5238.0, 6719.0),
                mutableListOf<Double>(1.06, 0.82, 1.1),
            ),
            FlaggedGlobalTransitTimeLedgerAdjuster(flags, "global-transit-time-ledger-regional", -5.3),
            GlobalTransitTimeLedgerExpiresAtOffsetAdjuster(1.94),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 54303.4,
    )
}
