package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.flags.Registry

/** One step of the capped trailer yard rollup calculation. */
interface CappedTrailerYardRollupAdjuster {
    fun describe(): String
    fun adjust(item: CappedTrailerYardRollup, value: Double): Double
}

data class FlaggedCappedTrailerYardRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTrailerYardRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTrailerYardRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCappedTrailerYardRollupAdjuster(var minimum: Double) : CappedTrailerYardRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTrailerYardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedTrailerYardRollupPalletsOffsetAdjuster(var weight: Double) : CappedTrailerYardRollupAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CappedTrailerYardRollup, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentCappedTrailerYardRollupAdjuster(var percent: Double) : CappedTrailerYardRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTrailerYardRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped trailer yard rollup steps in order and keeps a trace of each. */
data class CappedTrailerYardRollupEngine(
    var steps: MutableList<CappedTrailerYardRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedTrailerYardRollup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedTrailerYardRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedTrailerYardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedTrailerYardRollupEngine(flags: Registry): CappedTrailerYardRollupEngine {
    return CappedTrailerYardRollupEngine(
        steps = mutableListOf<CappedTrailerYardRollupAdjuster>(
            FlaggedCappedTrailerYardRollupAdjuster(flags, "capped-trailer-yard-rollup-strict", 6.4),
            MinimumCappedTrailerYardRollupAdjuster(45.2),
            CappedTrailerYardRollupPalletsOffsetAdjuster(0.35),
            PercentCappedTrailerYardRollupAdjuster(23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 82071.5,
    )
}
