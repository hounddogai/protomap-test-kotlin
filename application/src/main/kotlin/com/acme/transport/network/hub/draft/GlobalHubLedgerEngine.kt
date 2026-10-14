package com.acme.transport.network.hub.draft

import com.acme.transport.kit.numbers.roundTo

/** One step of the global hub ledger calculation. */
interface GlobalHubLedgerModifier {
    fun describe(): String
    fun adjust(item: GlobalHubLedger, value: Double): Double
}

data class CapGlobalHubLedgerAdjuster(var cap: Double) : GlobalHubLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalHubLedger, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalHubLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalHubLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalHubLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalHubLedgerAdjuster(var step: Double) : GlobalHubLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalHubLedger, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalHubLedgerAdjuster(var minimum: Double) : GlobalHubLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalHubLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalHubLedgerAdjuster(var percent: Double) : GlobalHubLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalHubLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalHubLedgerCreatedAtOffsetAdjuster(var weight: Double) : GlobalHubLedgerModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: GlobalHubLedger, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the global hub ledger steps in order and keeps a trace of each. */
data class GlobalHubLedgerEngine(
    var steps: MutableList<GlobalHubLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalHubLedger): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalHubLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalHubLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalHubLedgerEngine(): GlobalHubLedgerEngine {
    return GlobalHubLedgerEngine(
        steps = mutableListOf<GlobalHubLedgerModifier>(
            CapGlobalHubLedgerAdjuster(37343.0),
            TieredGlobalHubLedgerAdjuster(
                mutableListOf<Double>(1552.0, 4593.0, 8139.0, 8564.0),
                mutableListOf<Double>(1.07, 0.9, 1.07, 1.14),
            ),
            RoundingGlobalHubLedgerAdjuster(0.5),
            MinimumGlobalHubLedgerAdjuster(49.0),
            PercentGlobalHubLedgerAdjuster(1.4),
            GlobalHubLedgerCreatedAtOffsetAdjuster(0.17),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 40998.8,
    )
}
