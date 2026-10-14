package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel surcharge ledger calculation. */
interface FuelSurchargeLedgerStep {
    fun describe(): String
    fun adjust(item: FuelSurchargeLedger, value: Double): Double
}

data class MinimumFuelSurchargeLedgerAdjuster(var minimum: Double) : FuelSurchargeLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFuelSurchargeLedgerAdjuster(var step: Double) : FuelSurchargeLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeLedger, value: Double): Double = roundTo(value, step)
}

data class SegmentFuelSurchargeLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeLedgerStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: FuelSurchargeLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFuelSurchargeLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFuelSurchargeLedgerAdjuster(var cap: Double) : FuelSurchargeLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeLedger, value: Double): Double = minOf(value, cap)
}

data class FuelSurchargeLedgerScoreOffsetAdjuster(var weight: Double) : FuelSurchargeLedgerStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: FuelSurchargeLedger, value: Double): Double = value + item.score * weight
}

/** Runs the fuel surcharge ledger steps in order and keeps a trace of each. */
data class FuelSurchargeLedgerEngine(
    var steps: MutableList<FuelSurchargeLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeLedgerEngine(flags: Registry): FuelSurchargeLedgerEngine {
    return FuelSurchargeLedgerEngine(
        steps = mutableListOf<FuelSurchargeLedgerStep>(
            MinimumFuelSurchargeLedgerAdjuster(24.1),
            RoundingFuelSurchargeLedgerAdjuster(0.5),
            SegmentFuelSurchargeLedgerAdjuster(
                mutableMapOf<String, Double>("north" to 0.96, "west" to 1.23, "east" to 0.97),
            ),
            FlaggedFuelSurchargeLedgerAdjuster(flags, "fuel-surcharge-ledger-default", -2.0),
            CapFuelSurchargeLedgerAdjuster(7130.0),
            FuelSurchargeLedgerScoreOffsetAdjuster(0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 73421.4,
    )
}
