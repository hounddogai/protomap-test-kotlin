package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered fuel burn ledger calculation. */
interface TieredFuelBurnLedgerAdjuster {
    fun describe(): String
    fun adjust(item: TieredFuelBurnLedger, value: Double): Double
}

data class CapTieredFuelBurnLedgerAdjuster(var cap: Double) : TieredFuelBurnLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredFuelBurnLedger, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredFuelBurnLedgerAdjuster(var step: Double) : TieredFuelBurnLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredFuelBurnLedger, value: Double): Double = roundTo(value, step)
}

data class PercentTieredFuelBurnLedgerAdjuster(var percent: Double) : TieredFuelBurnLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredFuelBurnLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredFuelBurnLedgerAdjuster(var minimum: Double) : TieredFuelBurnLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredFuelBurnLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredFuelBurnLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredFuelBurnLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredFuelBurnLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered fuel burn ledger steps in order and keeps a trace of each. */
data class TieredFuelBurnLedgerEngine(
    var steps: MutableList<TieredFuelBurnLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredFuelBurnLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredFuelBurnLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredFuelBurnLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredFuelBurnLedgerEngine(): TieredFuelBurnLedgerEngine {
    return TieredFuelBurnLedgerEngine(
        steps = mutableListOf<TieredFuelBurnLedgerAdjuster>(
            CapTieredFuelBurnLedgerAdjuster(14309.0),
            RoundingTieredFuelBurnLedgerAdjuster(5.0),
            PercentTieredFuelBurnLedgerAdjuster(-10.6),
            MinimumTieredFuelBurnLedgerAdjuster(2.5),
            TieredTieredFuelBurnLedgerAdjuster(
                mutableListOf<Double>(977.0, 7302.0),
                mutableListOf<Double>(1.09, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 31674.8,
    )
}
