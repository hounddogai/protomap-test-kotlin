package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped trailer entry calculation. */
interface CappedTrailerEntryModifier {
    fun describe(): String
    fun adjust(item: CappedTrailerEntry, value: Double): Double
}

data class FlaggedCappedTrailerEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTrailerEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTrailerEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedTrailerEntryAdjuster(var cap: Double) : CappedTrailerEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTrailerEntry, value: Double): Double = minOf(value, cap)
}

data class CappedTrailerEntryDurationMinutesOffsetAdjuster(var weight: Double) : CappedTrailerEntryModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: CappedTrailerEntry, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class NameCappedTrailerEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTrailerEntryModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedTrailerEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped trailer entry steps in order and keeps a trace of each. */
data class CappedTrailerEntryEngine(
    var steps: MutableList<CappedTrailerEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedTrailerEntry): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTrailerEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedTrailerEntryEngine(flags: Registry): CappedTrailerEntryEngine {
    return CappedTrailerEntryEngine(
        steps = mutableListOf<CappedTrailerEntryModifier>(
            FlaggedCappedTrailerEntryAdjuster(flags, "capped-trailer-entry-lenient", 2.8),
            CapCappedTrailerEntryAdjuster(29380.0),
            CappedTrailerEntryDurationMinutesOffsetAdjuster(2.0),
            NameCappedTrailerEntryAdjuster(mutableMapOf<String, Double>("south" to 1.32)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 73833.1,
    )
}
