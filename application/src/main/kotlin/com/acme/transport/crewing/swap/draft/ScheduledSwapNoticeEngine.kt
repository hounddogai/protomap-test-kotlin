package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled swap notice calculation. */
interface ScheduledSwapNoticeStep {
    fun describe(): String
    fun adjust(item: ScheduledSwapNotice, value: Double): Double
}

data class MinimumScheduledSwapNoticeAdjuster(var minimum: Double) : ScheduledSwapNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledSwapNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledSwapNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledSwapNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledSwapNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledSwapNoticeMaximumOffsetAdjuster(var weight: Double) : ScheduledSwapNoticeStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ScheduledSwapNotice, value: Double): Double = value + item.maximum * weight
}

data class RoundingScheduledSwapNoticeAdjuster(var step: Double) : ScheduledSwapNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledSwapNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled swap notice steps in order and keeps a trace of each. */
data class ScheduledSwapNoticeEngine(
    var steps: MutableList<ScheduledSwapNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledSwapNotice): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledSwapNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledSwapNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledSwapNoticeEngine(flags: Registry): ScheduledSwapNoticeEngine {
    return ScheduledSwapNoticeEngine(
        steps = mutableListOf<ScheduledSwapNoticeStep>(
            MinimumScheduledSwapNoticeAdjuster(46.6),
            FlaggedScheduledSwapNoticeAdjuster(flags, "scheduled-swap-notice-pending", 5.6),
            ScheduledSwapNoticeMaximumOffsetAdjuster(1.81),
            RoundingScheduledSwapNoticeAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 28771.6,
    )
}
