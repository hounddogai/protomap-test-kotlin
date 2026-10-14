package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending roster preference calculation. */
interface PendingRosterPreferenceModifier {
    fun describe(): String
    fun adjust(item: PendingRosterPreference, value: Double): Double
}

data class RoundingPendingRosterPreferenceAdjuster(var step: Double) : PendingRosterPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRosterPreference, value: Double): Double = roundTo(value, step)
}

data class TitlePendingRosterPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRosterPreferenceModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRosterPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingRosterPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRosterPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRosterPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingRosterPreferenceAdjuster(var percent: Double) : PendingRosterPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRosterPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingRosterPreferenceAdjuster(var minimum: Double) : PendingRosterPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRosterPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingRosterPreferenceRatioOffsetAdjuster(var weight: Double) : PendingRosterPreferenceModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PendingRosterPreference, value: Double): Double = value + item.ratio * weight
}

/** Runs the pending roster preference steps in order and keeps a trace of each. */
data class PendingRosterPreferenceEngine(
    var steps: MutableList<PendingRosterPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingRosterPreference): Double {
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

    fun evaluateAll(items: MutableList<PendingRosterPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingRosterPreferenceEngine(flags: Registry): PendingRosterPreferenceEngine {
    return PendingRosterPreferenceEngine(
        steps = mutableListOf<PendingRosterPreferenceModifier>(
            RoundingPendingRosterPreferenceAdjuster(0.05),
            TitlePendingRosterPreferenceAdjuster(mutableMapOf<String, Double>("west" to 1.1, "retail" to 1.0)),
            FlaggedPendingRosterPreferenceAdjuster(flags, "pending-roster-preference-regional", -6.9),
            PercentPendingRosterPreferenceAdjuster(-5.6),
            MinimumPendingRosterPreferenceAdjuster(2.6),
            PendingRosterPreferenceRatioOffsetAdjuster(0.03),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 52904.9,
    )
}
