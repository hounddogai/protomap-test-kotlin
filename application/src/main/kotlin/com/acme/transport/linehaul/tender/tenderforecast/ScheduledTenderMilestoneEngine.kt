package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled tender milestone calculation. */
interface ScheduledTenderMilestoneModifier {
    fun describe(): String
    fun adjust(item: ScheduledTenderMilestone, value: Double): Double
}

data class LocaleScheduledTenderMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledTenderMilestoneModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: ScheduledTenderMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledTenderMilestoneVersionOffsetAdjuster(
    var weight: Double,
) : ScheduledTenderMilestoneModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ScheduledTenderMilestone, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class TieredScheduledTenderMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledTenderMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledTenderMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledTenderMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTenderMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTenderMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledTenderMilestoneAdjuster(var cap: Double) : ScheduledTenderMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTenderMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled tender milestone steps in order and keeps a trace of each. */
data class ScheduledTenderMilestoneEngine(
    var steps: MutableList<ScheduledTenderMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledTenderMilestone): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTenderMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTenderMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledTenderMilestoneEngine(flags: Registry): ScheduledTenderMilestoneEngine {
    return ScheduledTenderMilestoneEngine(
        steps = mutableListOf<ScheduledTenderMilestoneModifier>(
            LocaleScheduledTenderMilestoneAdjuster(
                mutableMapOf<String, Double>("online" to 0.82, "south" to 1.3, "retail" to 1.31),
            ),
            ScheduledTenderMilestoneVersionOffsetAdjuster(0.98),
            TieredScheduledTenderMilestoneAdjuster(
                mutableListOf<Double>(6444.0, 8172.0, 9699.0),
                mutableListOf<Double>(0.88, 0.8, 1.02),
            ),
            FlaggedScheduledTenderMilestoneAdjuster(flags, "scheduled-tender-milestone-regional", 6.1),
            CapScheduledTenderMilestoneAdjuster(39799.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 26263.0,
    )
}
