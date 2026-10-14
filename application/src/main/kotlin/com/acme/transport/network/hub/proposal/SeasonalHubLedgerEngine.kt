package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal hub ledger calculation. */
interface SeasonalHubLedgerAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalHubLedger, value: Double): Double
}

data class CapSeasonalHubLedgerAdjuster(var cap: Double) : SeasonalHubLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalHubLedger, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalHubLedgerAdjuster(var minimum: Double) : SeasonalHubLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalHubLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalHubLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalHubLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalHubLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneSeasonalHubLedgerAdjuster(var factors: MutableMap<String, Double>) : SeasonalHubLedgerAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: SeasonalHubLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal hub ledger steps in order and keeps a trace of each. */
data class SeasonalHubLedgerEngine(
    var steps: MutableList<SeasonalHubLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalHubLedger): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalHubLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalHubLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalHubLedgerEngine(flags: Registry): SeasonalHubLedgerEngine {
    return SeasonalHubLedgerEngine(
        steps = mutableListOf<SeasonalHubLedgerAdjuster>(
            CapSeasonalHubLedgerAdjuster(16381.0),
            MinimumSeasonalHubLedgerAdjuster(45.4),
            FlaggedSeasonalHubLedgerAdjuster(flags, "seasonal-hub-ledger-weighted", 6.1),
            LaneSeasonalHubLedgerAdjuster(mutableMapOf<String, Double>("north" to 1.33, "south" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 44791.2,
    )
}
