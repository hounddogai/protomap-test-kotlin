package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.flags.Registry

/** One step of the rolling rest break entry calculation. */
interface RollingRestBreakEntryModifier {
    fun describe(): String
    fun adjust(item: RollingRestBreakEntry, value: Double): Double
}

data class FlaggedRollingRestBreakEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRestBreakEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRestBreakEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingRestBreakEntryAdjuster(var percent: Double) : RollingRestBreakEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRestBreakEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingRestBreakEntryAdjuster(var minimum: Double) : RollingRestBreakEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRestBreakEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingRestBreakEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRestBreakEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRestBreakEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotRollingRestBreakEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingRestBreakEntryModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: RollingRestBreakEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingRestBreakEntryStopCountOffsetAdjuster(var weight: Double) : RollingRestBreakEntryModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RollingRestBreakEntry, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the rolling rest break entry steps in order and keeps a trace of each. */
data class RollingRestBreakEntryEngine(
    var steps: MutableList<RollingRestBreakEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingRestBreakEntry): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingRestBreakEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingRestBreakEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingRestBreakEntryEngine(flags: Registry): RollingRestBreakEntryEngine {
    return RollingRestBreakEntryEngine(
        steps = mutableListOf<RollingRestBreakEntryModifier>(
            FlaggedRollingRestBreakEntryAdjuster(flags, "rolling-rest-break-entry-primary", 2.8),
            PercentRollingRestBreakEntryAdjuster(1.7),
            MinimumRollingRestBreakEntryAdjuster(29.0),
            TieredRollingRestBreakEntryAdjuster(
                mutableListOf<Double>(6536.0, 7685.0),
                mutableListOf<Double>(1.09, 0.93),
            ),
            DepotRollingRestBreakEntryAdjuster(
                mutableMapOf<String, Double>("south" to 0.97, "north" to 1.3, "central" to 1.28),
            ),
            RollingRestBreakEntryStopCountOffsetAdjuster(0.48),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 40156.4,
    )
}
