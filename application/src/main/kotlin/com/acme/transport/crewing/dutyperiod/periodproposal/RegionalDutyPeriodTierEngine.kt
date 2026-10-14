package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional duty period tier calculation. */
interface RegionalDutyPeriodTierAdjuster {
    fun describe(): String
    fun adjust(item: RegionalDutyPeriodTier, value: Double): Double
}

data class RoundingRegionalDutyPeriodTierAdjuster(var step: Double) : RegionalDutyPeriodTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDutyPeriodTier, value: Double): Double = roundTo(value, step)
}

data class CapRegionalDutyPeriodTierAdjuster(var cap: Double) : RegionalDutyPeriodTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDutyPeriodTier, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalDutyPeriodTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDutyPeriodTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDutyPeriodTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalDutyPeriodTierAdjuster(var percent: Double) : RegionalDutyPeriodTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDutyPeriodTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalDutyPeriodTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDutyPeriodTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDutyPeriodTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional duty period tier steps in order and keeps a trace of each. */
data class RegionalDutyPeriodTierEngine(
    var steps: MutableList<RegionalDutyPeriodTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDutyPeriodTier): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDutyPeriodTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDutyPeriodTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDutyPeriodTierEngine(flags: Registry): RegionalDutyPeriodTierEngine {
    return RegionalDutyPeriodTierEngine(
        steps = mutableListOf<RegionalDutyPeriodTierAdjuster>(
            RoundingRegionalDutyPeriodTierAdjuster(1.0),
            CapRegionalDutyPeriodTierAdjuster(36796.0),
            FlaggedRegionalDutyPeriodTierAdjuster(flags, "regional-duty-period-tier-fallback", 3.7),
            PercentRegionalDutyPeriodTierAdjuster(-11.7),
            TieredRegionalDutyPeriodTierAdjuster(
                mutableListOf<Double>(1516.0, 2626.0, 3008.0, 5040.0),
                mutableListOf<Double>(1.01, 1.02, 0.86, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 35954.3,
    )
}
