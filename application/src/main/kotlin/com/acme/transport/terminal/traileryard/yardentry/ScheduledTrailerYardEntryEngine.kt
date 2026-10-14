package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled trailer yard entry calculation. */
interface ScheduledTrailerYardEntryAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledTrailerYardEntry, value: Double): Double
}

data class NameScheduledTrailerYardEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledTrailerYardEntryAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledTrailerYardEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledTrailerYardEntryAdjuster(var cap: Double) : ScheduledTrailerYardEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTrailerYardEntry, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledTrailerYardEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTrailerYardEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTrailerYardEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledTrailerYardEntryAdjuster(var minimum: Double) : ScheduledTrailerYardEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledTrailerYardEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledTrailerYardEntryAdjuster(var step: Double) : ScheduledTrailerYardEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledTrailerYardEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled trailer yard entry steps in order and keeps a trace of each. */
data class ScheduledTrailerYardEntryEngine(
    var steps: MutableList<ScheduledTrailerYardEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledTrailerYardEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledTrailerYardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledTrailerYardEntryEngine(flags: Registry): ScheduledTrailerYardEntryEngine {
    return ScheduledTrailerYardEntryEngine(
        steps = mutableListOf<ScheduledTrailerYardEntryAdjuster>(
            NameScheduledTrailerYardEntryAdjuster(mutableMapOf<String, Double>("north" to 1.15)),
            CapScheduledTrailerYardEntryAdjuster(19288.0),
            FlaggedScheduledTrailerYardEntryAdjuster(flags, "scheduled-trailer-yard-entry-batched", -0.2),
            MinimumScheduledTrailerYardEntryAdjuster(14.3),
            RoundingScheduledTrailerYardEntryAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 22929.9,
    )
}
