package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending idling entry calculation. */
interface PendingIdlingEntryAdjuster {
    fun describe(): String
    fun adjust(item: PendingIdlingEntry, value: Double): Double
}

data class RoundingPendingIdlingEntryAdjuster(var step: Double) : PendingIdlingEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingIdlingEntry, value: Double): Double = roundTo(value, step)
}

data class CapPendingIdlingEntryAdjuster(var cap: Double) : PendingIdlingEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingIdlingEntry, value: Double): Double = minOf(value, cap)
}

data class PercentPendingIdlingEntryAdjuster(var percent: Double) : PendingIdlingEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingIdlingEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingIdlingEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingIdlingEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingIdlingEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingIdlingEntryAdjuster(var minimum: Double) : PendingIdlingEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingIdlingEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending idling entry steps in order and keeps a trace of each. */
data class PendingIdlingEntryEngine(
    var steps: MutableList<PendingIdlingEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingIdlingEntry): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingIdlingEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingIdlingEntryEngine(flags: Registry): PendingIdlingEntryEngine {
    return PendingIdlingEntryEngine(
        steps = mutableListOf<PendingIdlingEntryAdjuster>(
            RoundingPendingIdlingEntryAdjuster(5.0),
            CapPendingIdlingEntryAdjuster(27359.0),
            PercentPendingIdlingEntryAdjuster(20.8),
            FlaggedPendingIdlingEntryAdjuster(flags, "pending-idling-entry-primary", -8.8),
            MinimumPendingIdlingEntryAdjuster(29.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 4620.3,
    )
}
