package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.flags.Registry

/** One step of the capped hold entry calculation. */
interface CappedHoldEntryFactor {
    fun describe(): String
    fun adjust(item: CappedHoldEntry, value: Double): Double
}

data class MinimumCappedHoldEntryAdjuster(var minimum: Double) : CappedHoldEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedHoldEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedHoldEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedHoldEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedHoldEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedHoldEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHoldEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHoldEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedHoldEntryAdjuster(var percent: Double) : CappedHoldEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedHoldEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped hold entry steps in order and keeps a trace of each. */
data class CappedHoldEntryEngine(
    var steps: MutableList<CappedHoldEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedHoldEntry): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedHoldEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedHoldEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedHoldEntryEngine(flags: Registry): CappedHoldEntryEngine {
    return CappedHoldEntryEngine(
        steps = mutableListOf<CappedHoldEntryFactor>(
            MinimumCappedHoldEntryAdjuster(6.8),
            FlaggedCappedHoldEntryAdjuster(flags, "capped-hold-entry-manual", -4.5),
            TieredCappedHoldEntryAdjuster(
                mutableListOf<Double>(1798.0, 4195.0, 4206.0),
                mutableListOf<Double>(1.16, 1.07, 1.18),
            ),
            PercentCappedHoldEntryAdjuster(9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 82099.7,
    )
}
