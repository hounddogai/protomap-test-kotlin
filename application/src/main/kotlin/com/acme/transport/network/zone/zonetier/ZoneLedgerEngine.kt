package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone ledger calculation. */
interface ZoneLedgerModifier {
    fun describe(): String
    fun adjust(item: ZoneLedger, value: Double): Double
}

data class MinimumZoneLedgerAdjuster(var minimum: Double) : ZoneLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingZoneLedgerAdjuster(var step: Double) : ZoneLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneLedger, value: Double): Double = roundTo(value, step)
}

data class ZoneLedgerBudgetOffsetAdjuster(var weight: Double) : ZoneLedgerModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ZoneLedger, value: Double): Double = value + item.budget * weight
}

data class PercentZoneLedgerAdjuster(var percent: Double) : ZoneLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapZoneLedgerAdjuster(var cap: Double) : ZoneLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneLedger, value: Double): Double = minOf(value, cap)
}

data class TieredZoneLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the zone ledger steps in order and keeps a trace of each. */
data class ZoneLedgerEngine(
    var steps: MutableList<ZoneLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneLedgerEngine(): ZoneLedgerEngine {
    return ZoneLedgerEngine(
        steps = mutableListOf<ZoneLedgerModifier>(
            MinimumZoneLedgerAdjuster(47.9),
            RoundingZoneLedgerAdjuster(0.05),
            ZoneLedgerBudgetOffsetAdjuster(1.19),
            PercentZoneLedgerAdjuster(12.7),
            CapZoneLedgerAdjuster(9077.0),
            TieredZoneLedgerAdjuster(
                mutableListOf<Double>(2043.0, 4546.0, 6149.0, 6896.0),
                mutableListOf<Double>(1.01, 1.09, 1.03, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 52570.1,
    )
}
