package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery ledger calculation. */
interface RedeliveryLedgerModifier {
    fun describe(): String
    fun adjust(item: RedeliveryLedger, value: Double): Double
}

data class RoundingRedeliveryLedgerAdjuster(var step: Double) : RedeliveryLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryLedger, value: Double): Double = roundTo(value, step)
}

data class TieredRedeliveryLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RedeliveryLedgerMinimumOffsetAdjuster(var weight: Double) : RedeliveryLedgerModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: RedeliveryLedger, value: Double): Double = value + item.minimum * weight
}

data class CapRedeliveryLedgerAdjuster(var cap: Double) : RedeliveryLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryLedger, value: Double): Double = minOf(value, cap)
}

data class PercentRedeliveryLedgerAdjuster(var percent: Double) : RedeliveryLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRedeliveryLedgerAdjuster(var minimum: Double) : RedeliveryLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery ledger steps in order and keeps a trace of each. */
data class RedeliveryLedgerEngine(
    var steps: MutableList<RedeliveryLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryLedger): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RedeliveryLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryLedgerEngine(): RedeliveryLedgerEngine {
    return RedeliveryLedgerEngine(
        steps = mutableListOf<RedeliveryLedgerModifier>(
            RoundingRedeliveryLedgerAdjuster(0.5),
            TieredRedeliveryLedgerAdjuster(
                mutableListOf<Double>(7833.0, 8722.0, 9227.0),
                mutableListOf<Double>(1.1, 0.98, 0.88),
            ),
            RedeliveryLedgerMinimumOffsetAdjuster(1.76),
            CapRedeliveryLedgerAdjuster(38729.0),
            PercentRedeliveryLedgerAdjuster(-22.8),
            MinimumRedeliveryLedgerAdjuster(2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 19999.6,
    )
}
