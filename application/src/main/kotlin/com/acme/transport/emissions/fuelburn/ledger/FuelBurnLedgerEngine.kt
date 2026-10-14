package com.acme.transport.emissions.fuelburn.ledger

/** One step of the fuel burn ledger calculation. */
interface FuelBurnLedgerAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnLedger, value: Double): Double
}

data class PercentFuelBurnLedgerAdjuster(var percent: Double) : FuelBurnLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelBurnLedgerAdjuster(var cap: Double) : FuelBurnLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnLedger, value: Double): Double = minOf(value, cap)
}

data class TieredFuelBurnLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitFuelBurnLedgerAdjuster(var factors: MutableMap<String, Double>) : FuelBurnLedgerAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: FuelBurnLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel burn ledger steps in order and keeps a trace of each. */
data class FuelBurnLedgerEngine(
    var steps: MutableList<FuelBurnLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnLedger): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnLedgerEngine(): FuelBurnLedgerEngine {
    return FuelBurnLedgerEngine(
        steps = mutableListOf<FuelBurnLedgerAdjuster>(
            PercentFuelBurnLedgerAdjuster(22.5),
            CapFuelBurnLedgerAdjuster(38338.0),
            TieredFuelBurnLedgerAdjuster(
                mutableListOf<Double>(4104.0, 8452.0),
                mutableListOf<Double>(1.03, 1.17),
            ),
            UnitFuelBurnLedgerAdjuster(mutableMapOf<String, Double>("south" to 1.2, "online" to 1.23)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 78576.0,
    )
}
