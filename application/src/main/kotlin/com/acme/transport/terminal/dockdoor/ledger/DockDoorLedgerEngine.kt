package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door ledger calculation. */
interface DockDoorLedgerAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorLedger, value: Double): Double
}

data class PercentDockDoorLedgerAdjuster(var percent: Double) : DockDoorLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDockDoorLedgerAdjuster(var step: Double) : DockDoorLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorLedger, value: Double): Double = roundTo(value, step)
}

data class TieredDockDoorLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dock door ledger steps in order and keeps a trace of each. */
data class DockDoorLedgerEngine(
    var steps: MutableList<DockDoorLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DockDoorLedger): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDockDoorLedgerEngine(): DockDoorLedgerEngine {
    return DockDoorLedgerEngine(
        steps = mutableListOf<DockDoorLedgerAdjuster>(
            PercentDockDoorLedgerAdjuster(-13.5),
            RoundingDockDoorLedgerAdjuster(0.25),
            TieredDockDoorLedgerAdjuster(
                mutableListOf<Double>(925.0, 3743.0, 8713.0),
                mutableListOf<Double>(0.89, 0.85, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 80687.0,
    )
}
