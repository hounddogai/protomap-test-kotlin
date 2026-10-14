package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual cutoff ledger calculation. */
interface ManualCutoffLedgerModifier {
    fun describe(): String
    fun adjust(item: ManualCutoffLedger, value: Double): Double
}

data class PercentManualCutoffLedgerAdjuster(var percent: Double) : ManualCutoffLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCutoffLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualCutoffLedgerAdjuster(var minimum: Double) : ManualCutoffLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCutoffLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualCutoffLedgerPalletsOffsetAdjuster(var weight: Double) : ManualCutoffLedgerModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ManualCutoffLedger, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TieredManualCutoffLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCutoffLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCutoffLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual cutoff ledger steps in order and keeps a trace of each. */
data class ManualCutoffLedgerEngine(
    var steps: MutableList<ManualCutoffLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualCutoffLedger): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCutoffLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualCutoffLedgerEngine(): ManualCutoffLedgerEngine {
    return ManualCutoffLedgerEngine(
        steps = mutableListOf<ManualCutoffLedgerModifier>(
            PercentManualCutoffLedgerAdjuster(-1.4),
            MinimumManualCutoffLedgerAdjuster(40.5),
            ManualCutoffLedgerPalletsOffsetAdjuster(0.62),
            TieredManualCutoffLedgerAdjuster(
                mutableListOf<Double>(9346.0, 9823.0),
                mutableListOf<Double>(1.17, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 64809.7,
    )
}
