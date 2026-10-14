package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict swap ledger calculation. */
interface StrictSwapLedgerModifier {
    fun describe(): String
    fun adjust(item: StrictSwapLedger, value: Double): Double
}

data class CapStrictSwapLedgerAdjuster(var cap: Double) : StrictSwapLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictSwapLedger, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictSwapLedgerAdjuster(var step: Double) : StrictSwapLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictSwapLedger, value: Double): Double = roundTo(value, step)
}

data class NameStrictSwapLedgerAdjuster(var factors: MutableMap<String, Double>) : StrictSwapLedgerModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictSwapLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictSwapLedgerAdjuster(var percent: Double) : StrictSwapLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictSwapLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictSwapLedgerBatchSizeOffsetAdjuster(var weight: Double) : StrictSwapLedgerModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: StrictSwapLedger, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class MinimumStrictSwapLedgerAdjuster(var minimum: Double) : StrictSwapLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictSwapLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict swap ledger steps in order and keeps a trace of each. */
data class StrictSwapLedgerEngine(
    var steps: MutableList<StrictSwapLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictSwapLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictSwapLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictSwapLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictSwapLedgerEngine(): StrictSwapLedgerEngine {
    return StrictSwapLedgerEngine(
        steps = mutableListOf<StrictSwapLedgerModifier>(
            CapStrictSwapLedgerAdjuster(3403.0),
            RoundingStrictSwapLedgerAdjuster(0.05),
            NameStrictSwapLedgerAdjuster(mutableMapOf<String, Double>("east" to 1.16, "west" to 0.75)),
            PercentStrictSwapLedgerAdjuster(-15.0),
            StrictSwapLedgerBatchSizeOffsetAdjuster(0.22),
            MinimumStrictSwapLedgerAdjuster(47.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 1049.5,
    )
}
