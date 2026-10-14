package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled redelivery profile calculation. */
interface ScheduledRedeliveryProfileAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledRedeliveryProfile, value: Double): Double
}

data class TitleScheduledRedeliveryProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRedeliveryProfileAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledRedeliveryProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledRedeliveryProfileAdjuster(var step: Double) : ScheduledRedeliveryProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRedeliveryProfile, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledRedeliveryProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRedeliveryProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRedeliveryProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledRedeliveryProfileAdjuster(var minimum: Double) : ScheduledRedeliveryProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRedeliveryProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled redelivery profile steps in order and keeps a trace of each. */
data class ScheduledRedeliveryProfileEngine(
    var steps: MutableList<ScheduledRedeliveryProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledRedeliveryProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRedeliveryProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRedeliveryProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledRedeliveryProfileEngine(flags: Registry): ScheduledRedeliveryProfileEngine {
    return ScheduledRedeliveryProfileEngine(
        steps = mutableListOf<ScheduledRedeliveryProfileAdjuster>(
            TitleScheduledRedeliveryProfileAdjuster(
                mutableMapOf<String, Double>("online" to 1.29, "retail" to 1.39),
            ),
            RoundingScheduledRedeliveryProfileAdjuster(1.0),
            FlaggedScheduledRedeliveryProfileAdjuster(flags, "scheduled-redelivery-profile-lenient", 1.8),
            MinimumScheduledRedeliveryProfileAdjuster(23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 17643.9,
    )
}
