package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the duty period milestone calculation. */
interface DutyPeriodMilestoneModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodMilestone, value: Double): Double
}

data class PercentDutyPeriodMilestoneAdjuster(var percent: Double) : DutyPeriodMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleDutyPeriodMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : DutyPeriodMilestoneModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: DutyPeriodMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDutyPeriodMilestoneAdjuster(var step: Double) : DutyPeriodMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredDutyPeriodMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDutyPeriodMilestoneAdjuster(var minimum: Double) : DutyPeriodMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDutyPeriodMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the duty period milestone steps in order and keeps a trace of each. */
data class DutyPeriodMilestoneEngine(
    var steps: MutableList<DutyPeriodMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodMilestoneEngine(flags: Registry): DutyPeriodMilestoneEngine {
    return DutyPeriodMilestoneEngine(
        steps = mutableListOf<DutyPeriodMilestoneModifier>(
            PercentDutyPeriodMilestoneAdjuster(24.8),
            TitleDutyPeriodMilestoneAdjuster(mutableMapOf<String, Double>("south" to 1.39)),
            RoundingDutyPeriodMilestoneAdjuster(0.5),
            TieredDutyPeriodMilestoneAdjuster(
                mutableListOf<Double>(4826.0, 9168.0, 9621.0),
                mutableListOf<Double>(0.98, 0.85, 1.19),
            ),
            MinimumDutyPeriodMilestoneAdjuster(20.2),
            FlaggedDutyPeriodMilestoneAdjuster(flags, "duty-period-milestone-primary", 4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 51968.6,
    )
}
