package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the roster notice calculation. */
interface RosterNoticeAdjuster {
    fun describe(): String
    fun adjust(item: RosterNotice, value: Double): Double
}

data class RoundingRosterNoticeAdjuster(var step: Double) : RosterNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterNotice, value: Double): Double = roundTo(value, step)
}

data class FlaggedRosterNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRosterNoticeAdjuster(var minimum: Double) : RosterNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the roster notice steps in order and keeps a trace of each. */
data class RosterNoticeEngine(
    var steps: MutableList<RosterNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RosterNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterNoticeEngine(flags: Registry): RosterNoticeEngine {
    return RosterNoticeEngine(
        steps = mutableListOf<RosterNoticeAdjuster>(
            RoundingRosterNoticeAdjuster(0.25),
            FlaggedRosterNoticeAdjuster(flags, "roster-notice-legacy", -2.2),
            MinimumRosterNoticeAdjuster(17.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 9318.5,
    )
}
