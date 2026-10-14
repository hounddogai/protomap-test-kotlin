package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional zone ledger calculation. */
interface RegionalZoneLedgerStep {
    fun describe(): String
    fun adjust(item: RegionalZoneLedger, value: Double): Double
}

data class RoundingRegionalZoneLedgerAdjuster(var step: Double) : RegionalZoneLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalZoneLedger, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalZoneLedgerAdjuster(var minimum: Double) : RegionalZoneLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalZoneLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalZoneLedgerAdjuster(var percent: Double) : RegionalZoneLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalZoneLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryRegionalZoneLedgerAdjuster(var factors: MutableMap<String, Double>) : RegionalZoneLedgerStep {
    override fun describe(): String = "by country"

    override fun adjust(item: RegionalZoneLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalZoneLedgerQuantityOffsetAdjuster(var weight: Double) : RegionalZoneLedgerStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RegionalZoneLedger, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the regional zone ledger steps in order and keeps a trace of each. */
data class RegionalZoneLedgerEngine(
    var steps: MutableList<RegionalZoneLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalZoneLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalZoneLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalZoneLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalZoneLedgerEngine(): RegionalZoneLedgerEngine {
    return RegionalZoneLedgerEngine(
        steps = mutableListOf<RegionalZoneLedgerStep>(
            RoundingRegionalZoneLedgerAdjuster(0.25),
            MinimumRegionalZoneLedgerAdjuster(41.6),
            PercentRegionalZoneLedgerAdjuster(-3.9),
            CountryRegionalZoneLedgerAdjuster(
                mutableMapOf<String, Double>("retail" to 0.99, "west" to 1.04, "central" to 0.7),
            ),
            RegionalZoneLedgerQuantityOffsetAdjuster(0.72),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 50711.6,
    )
}
