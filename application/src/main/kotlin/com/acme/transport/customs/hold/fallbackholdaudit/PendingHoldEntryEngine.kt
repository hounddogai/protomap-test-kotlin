package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.flags.Registry

/** One step of the pending hold entry calculation. */
interface PendingHoldEntryModifier {
    fun describe(): String
    fun adjust(item: PendingHoldEntry, value: Double): Double
}

data class MinimumPendingHoldEntryAdjuster(var minimum: Double) : PendingHoldEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHoldEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingHoldEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingHoldEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingHoldEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingHoldEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingHoldEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingHoldEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending hold entry steps in order and keeps a trace of each. */
data class PendingHoldEntryEngine(
    var steps: MutableList<PendingHoldEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingHoldEntry): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingHoldEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingHoldEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingHoldEntryEngine(flags: Registry): PendingHoldEntryEngine {
    return PendingHoldEntryEngine(
        steps = mutableListOf<PendingHoldEntryModifier>(
            MinimumPendingHoldEntryAdjuster(5.8),
            FlaggedPendingHoldEntryAdjuster(flags, "pending-hold-entry-weighted", 1.5),
            TieredPendingHoldEntryAdjuster(
                mutableListOf<Double>(7281.0, 9877.0),
                mutableListOf<Double>(0.87, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 63021.8,
    )
}
