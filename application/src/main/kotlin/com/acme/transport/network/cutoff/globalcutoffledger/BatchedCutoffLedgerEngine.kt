package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched cutoff ledger calculation. */
interface BatchedCutoffLedgerStep {
    fun describe(): String
    fun adjust(item: BatchedCutoffLedger, value: Double): Double
}

data class RoundingBatchedCutoffLedgerAdjuster(var step: Double) : BatchedCutoffLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCutoffLedger, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedCutoffLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCutoffLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCutoffLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitBatchedCutoffLedgerAdjuster(var factors: MutableMap<String, Double>) : BatchedCutoffLedgerStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: BatchedCutoffLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedCutoffLedgerAdjuster(var percent: Double) : BatchedCutoffLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCutoffLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedCutoffLedgerAdjuster(var cap: Double) : BatchedCutoffLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCutoffLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the batched cutoff ledger steps in order and keeps a trace of each. */
data class BatchedCutoffLedgerEngine(
    var steps: MutableList<BatchedCutoffLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCutoffLedger): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCutoffLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCutoffLedgerEngine(): BatchedCutoffLedgerEngine {
    return BatchedCutoffLedgerEngine(
        steps = mutableListOf<BatchedCutoffLedgerStep>(
            RoundingBatchedCutoffLedgerAdjuster(1.0),
            TieredBatchedCutoffLedgerAdjuster(
                mutableListOf<Double>(3283.0, 5827.0, 9359.0, 9381.0),
                mutableListOf<Double>(0.82, 1.15, 0.99, 1.03),
            ),
            UnitBatchedCutoffLedgerAdjuster(mutableMapOf<String, Double>("online" to 1.38)),
            PercentBatchedCutoffLedgerAdjuster(1.4),
            CapBatchedCutoffLedgerAdjuster(19841.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 97744.0,
    )
}
