package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled rest break digest calculation. */
interface ScheduledRestBreakDigestAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledRestBreakDigest, value: Double): Double
}

data class RoundingScheduledRestBreakDigestAdjuster(var step: Double) : ScheduledRestBreakDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRestBreakDigest, value: Double): Double = roundTo(value, step)
}

data class CapScheduledRestBreakDigestAdjuster(var cap: Double) : ScheduledRestBreakDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRestBreakDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledRestBreakDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRestBreakDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRestBreakDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled rest break digest steps in order and keeps a trace of each. */
data class ScheduledRestBreakDigestEngine(
    var steps: MutableList<ScheduledRestBreakDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledRestBreakDigest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRestBreakDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRestBreakDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledRestBreakDigestEngine(flags: Registry): ScheduledRestBreakDigestEngine {
    return ScheduledRestBreakDigestEngine(
        steps = mutableListOf<ScheduledRestBreakDigestAdjuster>(
            RoundingScheduledRestBreakDigestAdjuster(0.25),
            CapScheduledRestBreakDigestAdjuster(49102.0),
            FlaggedScheduledRestBreakDigestAdjuster(flags, "scheduled-rest-break-digest-manual", 4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 26032.0,
    )
}
