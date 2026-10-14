package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the drop ledger calculation. */
interface DropLedgerAdjuster {
    fun describe(): String
    fun adjust(item: DropLedger, value: Double): Double
}

data class SegmentDropLedgerAdjuster(var factors: MutableMap<String, Double>) : DropLedgerAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: DropLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDropLedgerAdjuster(var cap: Double) : DropLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropLedger, value: Double): Double = minOf(value, cap)
}

data class RoundingDropLedgerAdjuster(var step: Double) : DropLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the drop ledger steps in order and keeps a trace of each. */
data class DropLedgerEngine(
    var steps: MutableList<DropLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DropLedger): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DropLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDropLedgerEngine(): DropLedgerEngine {
    return DropLedgerEngine(
        steps = mutableListOf<DropLedgerAdjuster>(
            SegmentDropLedgerAdjuster(mutableMapOf<String, Double>("west" to 0.93, "north" to 1.21)),
            CapDropLedgerAdjuster(9136.0),
            RoundingDropLedgerAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 78494.3,
    )
}
