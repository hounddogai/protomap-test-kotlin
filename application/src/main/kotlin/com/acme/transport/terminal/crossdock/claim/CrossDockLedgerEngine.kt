package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.flags.Registry

/** One step of the cross dock ledger calculation. */
interface CrossDockLedgerStep {
    fun describe(): String
    fun adjust(item: CrossDockLedger, value: Double): Double
}

data class FlaggedCrossDockLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCrossDockLedgerAdjuster(var minimum: Double) : CrossDockLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CrossDockLedgerRatioOffsetAdjuster(var weight: Double) : CrossDockLedgerStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: CrossDockLedger, value: Double): Double = value + item.ratio * weight
}

data class PercentCrossDockLedgerAdjuster(var percent: Double) : CrossDockLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionCrossDockLedgerAdjuster(var factors: MutableMap<String, Double>) : CrossDockLedgerStep {
    override fun describe(): String = "by region"

    override fun adjust(item: CrossDockLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cross dock ledger steps in order and keeps a trace of each. */
data class CrossDockLedgerEngine(
    var steps: MutableList<CrossDockLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockLedger): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockLedgerEngine(flags: Registry): CrossDockLedgerEngine {
    return CrossDockLedgerEngine(
        steps = mutableListOf<CrossDockLedgerStep>(
            FlaggedCrossDockLedgerAdjuster(flags, "cross-dock-ledger-lenient", 9.1),
            MinimumCrossDockLedgerAdjuster(15.4),
            CrossDockLedgerRatioOffsetAdjuster(1.41),
            PercentCrossDockLedgerAdjuster(-9.9),
            RegionCrossDockLedgerAdjuster(
                mutableMapOf<String, Double>("retail" to 0.98, "north" to 1.33, "east" to 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 45091.1,
    )
}
