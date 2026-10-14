package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled roster template calculation. */
interface ScheduledRosterTemplateAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledRosterTemplate, value: Double): Double
}

data class RoundingScheduledRosterTemplateAdjuster(var step: Double) : ScheduledRosterTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRosterTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledRosterTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRosterTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRosterTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledRosterTemplateAdjuster(var percent: Double) : ScheduledRosterTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRosterTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled roster template steps in order and keeps a trace of each. */
data class ScheduledRosterTemplateEngine(
    var steps: MutableList<ScheduledRosterTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledRosterTemplate): Double {
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

    fun evaluateAll(items: MutableList<ScheduledRosterTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledRosterTemplateEngine(flags: Registry): ScheduledRosterTemplateEngine {
    return ScheduledRosterTemplateEngine(
        steps = mutableListOf<ScheduledRosterTemplateAdjuster>(
            RoundingScheduledRosterTemplateAdjuster(0.25),
            FlaggedScheduledRosterTemplateAdjuster(flags, "scheduled-roster-template-strict", -9.6),
            PercentScheduledRosterTemplateAdjuster(8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 79648.6,
    )
}
