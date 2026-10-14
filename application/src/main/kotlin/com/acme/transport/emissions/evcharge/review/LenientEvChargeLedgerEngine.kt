package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient ev charge ledger calculation. */
interface LenientEvChargeLedgerStep {
    fun describe(): String
    fun adjust(item: LenientEvChargeLedger, value: Double): Double
}

data class CapLenientEvChargeLedgerAdjuster(var cap: Double) : LenientEvChargeLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientEvChargeLedger, value: Double): Double = minOf(value, cap)
}

data class TieredLenientEvChargeLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientEvChargeLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientEvChargeLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientEvChargeLedgerCostOffsetAdjuster(var weight: Double) : LenientEvChargeLedgerStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LenientEvChargeLedger, value: Double): Double = value + item.cost * weight
}

data class FlaggedLenientEvChargeLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientEvChargeLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientEvChargeLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientEvChargeLedgerAdjuster(var percent: Double) : LenientEvChargeLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientEvChargeLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient ev charge ledger steps in order and keeps a trace of each. */
data class LenientEvChargeLedgerEngine(
    var steps: MutableList<LenientEvChargeLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientEvChargeLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientEvChargeLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientEvChargeLedgerEngine(flags: Registry): LenientEvChargeLedgerEngine {
    return LenientEvChargeLedgerEngine(
        steps = mutableListOf<LenientEvChargeLedgerStep>(
            CapLenientEvChargeLedgerAdjuster(19450.0),
            TieredLenientEvChargeLedgerAdjuster(
                mutableListOf<Double>(1634.0, 3563.0, 3773.0, 8394.0),
                mutableListOf<Double>(0.91, 1.19, 0.95, 1.0),
            ),
            LenientEvChargeLedgerCostOffsetAdjuster(1.11),
            FlaggedLenientEvChargeLedgerAdjuster(flags, "lenient-ev-charge-ledger-batched", 6.6),
            PercentLenientEvChargeLedgerAdjuster(-24.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 88841.1,
    )
}
