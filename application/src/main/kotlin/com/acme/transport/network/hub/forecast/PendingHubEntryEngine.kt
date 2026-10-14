package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the pending hub entry calculation. */
interface PendingHubEntryFactor {
    fun describe(): String
    fun adjust(item: PendingHubEntry, value: Double): Double
}

data class FlaggedPendingHubEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingHubEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingHubEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingHubEntryAdjuster(var minimum: Double) : PendingHubEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHubEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingHubEntryAdjuster(var percent: Double) : PendingHubEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingHubEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending hub entry steps in order and keeps a trace of each. */
data class PendingHubEntryEngine(
    var steps: MutableList<PendingHubEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingHubEntry): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingHubEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingHubEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingHubEntryEngine(flags: Registry): PendingHubEntryEngine {
    return PendingHubEntryEngine(
        steps = mutableListOf<PendingHubEntryFactor>(
            FlaggedPendingHubEntryAdjuster(flags, "pending-hub-entry-weighted", 9.6),
            MinimumPendingHubEntryAdjuster(7.4),
            PercentPendingHubEntryAdjuster(22.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 98850.7,
    )
}
