package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.flags.Registry

/** One step of the primary transit time window calculation. */
interface PrimaryTransitTimeWindowModifier {
    fun describe(): String
    fun adjust(item: PrimaryTransitTimeWindow, value: Double): Double
}

data class FlaggedPrimaryTransitTimeWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTransitTimeWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTransitTimeWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryTransitTimeWindowAdjuster(var minimum: Double) : PrimaryTransitTimeWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTransitTimeWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryTransitTimeWindowBatchSizeOffsetAdjuster(
    var weight: Double,
) : PrimaryTransitTimeWindowModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PrimaryTransitTimeWindow, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CapPrimaryTransitTimeWindowAdjuster(var cap: Double) : PrimaryTransitTimeWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTransitTimeWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the primary transit time window steps in order and keeps a trace of each. */
data class PrimaryTransitTimeWindowEngine(
    var steps: MutableList<PrimaryTransitTimeWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTransitTimeWindow): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTransitTimeWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTransitTimeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTransitTimeWindowEngine(flags: Registry): PrimaryTransitTimeWindowEngine {
    return PrimaryTransitTimeWindowEngine(
        steps = mutableListOf<PrimaryTransitTimeWindowModifier>(
            FlaggedPrimaryTransitTimeWindowAdjuster(flags, "primary-transit-time-window-capped", 9.6),
            MinimumPrimaryTransitTimeWindowAdjuster(41.5),
            PrimaryTransitTimeWindowBatchSizeOffsetAdjuster(0.59),
            CapPrimaryTransitTimeWindowAdjuster(42608.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 85701.8,
    )
}
