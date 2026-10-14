package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled transit time entry calculation. */
interface ScheduledTransitTimeEntryFactor {
    fun describe(): String
    fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double
}

data class ScheduledTransitTimeEntryRatioOffsetAdjuster(var weight: Double) : ScheduledTransitTimeEntryFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double = value + item.ratio * weight
}

data class LabelScheduledTransitTimeEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledTransitTimeEntryFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledTransitTimeEntryAdjuster(var cap: Double) : ScheduledTransitTimeEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledTransitTimeEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledTransitTimeEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledTransitTimeEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTransitTimeEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledTransitTimeEntryAdjuster(var percent: Double) : ScheduledTransitTimeEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledTransitTimeEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled transit time entry steps in order and keeps a trace of each. */
data class ScheduledTransitTimeEntryEngine(
    var steps: MutableList<ScheduledTransitTimeEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledTransitTimeEntry): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledTransitTimeEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledTransitTimeEntryEngine(flags: Registry): ScheduledTransitTimeEntryEngine {
    return ScheduledTransitTimeEntryEngine(
        steps = mutableListOf<ScheduledTransitTimeEntryFactor>(
            ScheduledTransitTimeEntryRatioOffsetAdjuster(1.97),
            LabelScheduledTransitTimeEntryAdjuster(
                mutableMapOf<String, Double>("west" to 0.95, "north" to 0.9, "online" to 1.16),
            ),
            CapScheduledTransitTimeEntryAdjuster(26714.0),
            TieredScheduledTransitTimeEntryAdjuster(
                mutableListOf<Double>(31.0, 51.0, 3011.0),
                mutableListOf<Double>(1.1, 1.01, 0.87),
            ),
            FlaggedScheduledTransitTimeEntryAdjuster(flags, "scheduled-transit-time-entry-batched", 6.1),
            PercentScheduledTransitTimeEntryAdjuster(21.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 45642.4,
    )
}
