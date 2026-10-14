package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending coaching template calculation. */
interface PendingCoachingTemplateAdjuster {
    fun describe(): String
    fun adjust(item: PendingCoachingTemplate, value: Double): Double
}

data class RoundingPendingCoachingTemplateAdjuster(var step: Double) : PendingCoachingTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCoachingTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingCoachingTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCoachingTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCoachingTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionPendingCoachingTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCoachingTemplateAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingCoachingTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingCoachingTemplateDurationMinutesOffsetAdjuster(
    var weight: Double,
) : PendingCoachingTemplateAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: PendingCoachingTemplate, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class MinimumPendingCoachingTemplateAdjuster(var minimum: Double) : PendingCoachingTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCoachingTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending coaching template steps in order and keeps a trace of each. */
data class PendingCoachingTemplateEngine(
    var steps: MutableList<PendingCoachingTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingCoachingTemplate): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCoachingTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCoachingTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingCoachingTemplateEngine(flags: Registry): PendingCoachingTemplateEngine {
    return PendingCoachingTemplateEngine(
        steps = mutableListOf<PendingCoachingTemplateAdjuster>(
            RoundingPendingCoachingTemplateAdjuster(1.0),
            FlaggedPendingCoachingTemplateAdjuster(flags, "pending-coaching-template-pending", 6.7),
            RegionPendingCoachingTemplateAdjuster(mutableMapOf<String, Double>("east" to 1.04, "online" to 1.1)),
            PendingCoachingTemplateDurationMinutesOffsetAdjuster(1.27),
            MinimumPendingCoachingTemplateAdjuster(16.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 39340.4,
    )
}
