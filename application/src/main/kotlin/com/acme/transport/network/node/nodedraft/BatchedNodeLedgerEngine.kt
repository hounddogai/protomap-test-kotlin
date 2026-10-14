package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched node ledger calculation. */
interface BatchedNodeLedgerAdjuster {
    fun describe(): String
    fun adjust(item: BatchedNodeLedger, value: Double): Double
}

data class FlaggedBatchedNodeLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNodeLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNodeLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedNodeLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNodeLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNodeLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedNodeLedgerAdjuster(var step: Double) : BatchedNodeLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNodeLedger, value: Double): Double = roundTo(value, step)
}

data class DepotBatchedNodeLedgerAdjuster(var factors: MutableMap<String, Double>) : BatchedNodeLedgerAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedNodeLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedNodeLedgerDiscountOffsetAdjuster(var weight: Double) : BatchedNodeLedgerAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedNodeLedger, value: Double): Double = value + item.discount * weight
}

data class MinimumBatchedNodeLedgerAdjuster(var minimum: Double) : BatchedNodeLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNodeLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched node ledger steps in order and keeps a trace of each. */
data class BatchedNodeLedgerEngine(
    var steps: MutableList<BatchedNodeLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedNodeLedger): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedNodeLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedNodeLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedNodeLedgerEngine(flags: Registry): BatchedNodeLedgerEngine {
    return BatchedNodeLedgerEngine(
        steps = mutableListOf<BatchedNodeLedgerAdjuster>(
            FlaggedBatchedNodeLedgerAdjuster(flags, "batched-node-ledger-pending", 2.5),
            TieredBatchedNodeLedgerAdjuster(
                mutableListOf<Double>(2998.0, 6118.0),
                mutableListOf<Double>(0.98, 1.01),
            ),
            RoundingBatchedNodeLedgerAdjuster(0.05),
            DepotBatchedNodeLedgerAdjuster(mutableMapOf<String, Double>("north" to 0.92)),
            BatchedNodeLedgerDiscountOffsetAdjuster(1.73),
            MinimumBatchedNodeLedgerAdjuster(23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 80483.0,
    )
}
