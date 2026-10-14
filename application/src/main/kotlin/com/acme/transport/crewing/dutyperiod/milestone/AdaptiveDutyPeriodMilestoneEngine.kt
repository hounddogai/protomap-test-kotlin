package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive duty period milestone calculation. */
interface AdaptiveDutyPeriodMilestoneModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double
}

data class MinimumAdaptiveDutyPeriodMilestoneAdjuster(
    var minimum: Double,
) : AdaptiveDutyPeriodMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveDutyPeriodMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDutyPeriodMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveDutyPeriodMilestoneAdjuster(
    var percent: Double,
) : AdaptiveDutyPeriodMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveDutyPeriodMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDutyPeriodMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleAdaptiveDutyPeriodMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDutyPeriodMilestoneModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveDutyPeriodMilestoneAdjuster(var cap: Double) : AdaptiveDutyPeriodMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDutyPeriodMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive duty period milestone steps in order and keeps a trace of each. */
data class AdaptiveDutyPeriodMilestoneEngine(
    var steps: MutableList<AdaptiveDutyPeriodMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveDutyPeriodMilestone): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveDutyPeriodMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveDutyPeriodMilestoneEngine(flags: Registry): AdaptiveDutyPeriodMilestoneEngine {
    return AdaptiveDutyPeriodMilestoneEngine(
        steps = mutableListOf<AdaptiveDutyPeriodMilestoneModifier>(
            MinimumAdaptiveDutyPeriodMilestoneAdjuster(11.3),
            FlaggedAdaptiveDutyPeriodMilestoneAdjuster(flags, "adaptive-duty-period-milestone-manual", -0.8),
            PercentAdaptiveDutyPeriodMilestoneAdjuster(-7.7),
            TieredAdaptiveDutyPeriodMilestoneAdjuster(
                mutableListOf<Double>(2625.0, 9760.0),
                mutableListOf<Double>(1.1, 1.13),
            ),
            TitleAdaptiveDutyPeriodMilestoneAdjuster(
                mutableMapOf<String, Double>("online" to 1.0, "retail" to 1.3),
            ),
            CapAdaptiveDutyPeriodMilestoneAdjuster(23252.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 39976.5,
    )
}
