package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation ledger calculation. */
interface SortationLedgerFactor {
    fun describe(): String
    fun adjust(item: SortationLedger, value: Double): Double
}

data class CapSortationLedgerAdjuster(var cap: Double) : SortationLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationLedger, value: Double): Double = minOf(value, cap)
}

data class TieredSortationLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSortationLedgerAdjuster(var step: Double) : SortationLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationLedger, value: Double): Double = roundTo(value, step)
}

data class TitleSortationLedgerAdjuster(var factors: MutableMap<String, Double>) : SortationLedgerFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SortationLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the sortation ledger steps in order and keeps a trace of each. */
data class SortationLedgerEngine(
    var steps: MutableList<SortationLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationLedger): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SortationLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationLedgerEngine(): SortationLedgerEngine {
    return SortationLedgerEngine(
        steps = mutableListOf<SortationLedgerFactor>(
            CapSortationLedgerAdjuster(9960.0),
            TieredSortationLedgerAdjuster(
                mutableListOf<Double>(3518.0, 6277.0, 7655.0, 7992.0),
                mutableListOf<Double>(0.94, 1.2, 1.12, 1.12),
            ),
            RoundingSortationLedgerAdjuster(5.0),
            TitleSortationLedgerAdjuster(
                mutableMapOf<String, Double>("online" to 0.9, "south" to 1.07, "west" to 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 93782.1,
    )
}
