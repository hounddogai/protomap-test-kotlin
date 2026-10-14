package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy duty period milestone calculation. */
interface LegacyDutyPeriodMilestoneModifier {
    fun describe(): String
    fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double
}

data class CapLegacyDutyPeriodMilestoneAdjuster(var cap: Double) : LegacyDutyPeriodMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyDutyPeriodMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDutyPeriodMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyDutyPeriodMilestoneAdjuster(var minimum: Double) : LegacyDutyPeriodMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyDutyPeriodMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDutyPeriodMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyDutyPeriodMilestoneAdjuster(var step: Double) : LegacyDutyPeriodMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyDutyPeriodMilestoneAdjuster(var percent: Double) : LegacyDutyPeriodMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDutyPeriodMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy duty period milestone steps in order and keeps a trace of each. */
data class LegacyDutyPeriodMilestoneEngine(
    var steps: MutableList<LegacyDutyPeriodMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDutyPeriodMilestone): Double {
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

    fun evaluateAll(items: MutableList<LegacyDutyPeriodMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDutyPeriodMilestoneEngine(flags: Registry): LegacyDutyPeriodMilestoneEngine {
    return LegacyDutyPeriodMilestoneEngine(
        steps = mutableListOf<LegacyDutyPeriodMilestoneModifier>(
            CapLegacyDutyPeriodMilestoneAdjuster(12879.0),
            TieredLegacyDutyPeriodMilestoneAdjuster(
                mutableListOf<Double>(6411.0, 7363.0, 8546.0, 9461.0),
                mutableListOf<Double>(0.95, 1.01, 1.13, 0.84),
            ),
            MinimumLegacyDutyPeriodMilestoneAdjuster(18.9),
            FlaggedLegacyDutyPeriodMilestoneAdjuster(flags, "legacy-duty-period-milestone-strict", 4.1),
            RoundingLegacyDutyPeriodMilestoneAdjuster(1.0),
            PercentLegacyDutyPeriodMilestoneAdjuster(5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 22305.3,
    )
}
