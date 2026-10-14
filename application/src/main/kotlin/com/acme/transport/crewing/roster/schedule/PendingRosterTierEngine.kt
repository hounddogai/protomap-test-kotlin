package com.acme.transport.crewing.roster.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending roster tier calculation. */
interface PendingRosterTierFactor {
    fun describe(): String
    fun adjust(item: PendingRosterTier, value: Double): Double
}

data class PercentPendingRosterTierAdjuster(var percent: Double) : PendingRosterTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRosterTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingRosterTierAdjuster(var minimum: Double) : PendingRosterTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRosterTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingRosterTierStopCountOffsetAdjuster(var weight: Double) : PendingRosterTierFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PendingRosterTier, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class CapPendingRosterTierAdjuster(var cap: Double) : PendingRosterTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRosterTier, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingRosterTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRosterTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRosterTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotPendingRosterTierAdjuster(var factors: MutableMap<String, Double>) : PendingRosterTierFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: PendingRosterTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending roster tier steps in order and keeps a trace of each. */
data class PendingRosterTierEngine(
    var steps: MutableList<PendingRosterTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRosterTier): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingRosterTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRosterTierEngine(flags: Registry): PendingRosterTierEngine {
    return PendingRosterTierEngine(
        steps = mutableListOf<PendingRosterTierFactor>(
            PercentPendingRosterTierAdjuster(4.4),
            MinimumPendingRosterTierAdjuster(46.6),
            PendingRosterTierStopCountOffsetAdjuster(0.24),
            CapPendingRosterTierAdjuster(34613.0),
            FlaggedPendingRosterTierAdjuster(flags, "pending-roster-tier-scheduled", 9.4),
            DepotPendingRosterTierAdjuster(mutableMapOf<String, Double>("south" to 0.74, "east" to 0.88)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 21536.6,
    )
}
