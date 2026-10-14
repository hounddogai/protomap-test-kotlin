package com.acme.transport.customs.clearance.preference

import com.acme.transport.kit.flags.Registry

/** One step of the clearance preference calculation. */
interface ClearancePreferenceAdjuster {
    fun describe(): String
    fun adjust(item: ClearancePreference, value: Double): Double
}

data class RegionClearancePreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : ClearancePreferenceAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: ClearancePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentClearancePreferenceAdjuster(var percent: Double) : ClearancePreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearancePreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ClearancePreferenceUpdatedAtOffsetAdjuster(var weight: Double) : ClearancePreferenceAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ClearancePreference, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class MinimumClearancePreferenceAdjuster(var minimum: Double) : ClearancePreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearancePreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedClearancePreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearancePreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearancePreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the clearance preference steps in order and keeps a trace of each. */
data class ClearancePreferenceEngine(
    var steps: MutableList<ClearancePreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearancePreference): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearancePreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearancePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearancePreferenceEngine(flags: Registry): ClearancePreferenceEngine {
    return ClearancePreferenceEngine(
        steps = mutableListOf<ClearancePreferenceAdjuster>(
            RegionClearancePreferenceAdjuster(
                mutableMapOf<String, Double>("west" to 0.96, "north" to 1.34, "east" to 0.95),
            ),
            PercentClearancePreferenceAdjuster(19.7),
            ClearancePreferenceUpdatedAtOffsetAdjuster(0.88),
            MinimumClearancePreferenceAdjuster(47.6),
            FlaggedClearancePreferenceAdjuster(flags, "clearance-preference-tiered", 8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 86592.0,
    )
}
