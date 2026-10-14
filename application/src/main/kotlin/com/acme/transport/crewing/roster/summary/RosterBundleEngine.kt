package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster bundle calculation. */
interface RosterBundleAdjuster {
    fun describe(): String
    fun adjust(item: RosterBundle, value: Double): Double
}

data class TieredRosterBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRosterBundleAdjuster(var cap: Double) : RosterBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedRosterBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRosterBundleAdjuster(var minimum: Double) : RosterBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRosterBundleAdjuster(var percent: Double) : RosterBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster bundle steps in order and keeps a trace of each. */
data class RosterBundleEngine(
    var steps: MutableList<RosterBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RosterBundle): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRosterBundleEngine(flags: Registry): RosterBundleEngine {
    return RosterBundleEngine(
        steps = mutableListOf<RosterBundleAdjuster>(
            TieredRosterBundleAdjuster(
                mutableListOf<Double>(92.0, 639.0, 4548.0, 6839.0),
                mutableListOf<Double>(0.85, 1.05, 1.12, 1.06),
            ),
            CapRosterBundleAdjuster(10195.0),
            FlaggedRosterBundleAdjuster(flags, "roster-bundle-fallback", 5.6),
            MinimumRosterBundleAdjuster(20.8),
            PercentRosterBundleAdjuster(6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 27868.0,
    )
}
