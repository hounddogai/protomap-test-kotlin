package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional duty period profile calculation. */
interface RegionalDutyPeriodProfileFactor {
    fun describe(): String
    fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double
}

data class RoundingRegionalDutyPeriodProfileAdjuster(var step: Double) : RegionalDutyPeriodProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double = roundTo(value, step)
}

data class CapRegionalDutyPeriodProfileAdjuster(var cap: Double) : RegionalDutyPeriodProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalDutyPeriodProfileAdjuster(var percent: Double) : RegionalDutyPeriodProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalDutyPeriodProfileAdjuster(var minimum: Double) : RegionalDutyPeriodProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalDutyPeriodProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDutyPeriodProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalDutyPeriodProfileThresholdOffsetAdjuster(
    var weight: Double,
) : RegionalDutyPeriodProfileFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RegionalDutyPeriodProfile, value: Double): Double = value + item.threshold * weight
}

/** Runs the regional duty period profile steps in order and keeps a trace of each. */
data class RegionalDutyPeriodProfileEngine(
    var steps: MutableList<RegionalDutyPeriodProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDutyPeriodProfile): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDutyPeriodProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDutyPeriodProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDutyPeriodProfileEngine(flags: Registry): RegionalDutyPeriodProfileEngine {
    return RegionalDutyPeriodProfileEngine(
        steps = mutableListOf<RegionalDutyPeriodProfileFactor>(
            RoundingRegionalDutyPeriodProfileAdjuster(0.25),
            CapRegionalDutyPeriodProfileAdjuster(42709.0),
            PercentRegionalDutyPeriodProfileAdjuster(23.7),
            MinimumRegionalDutyPeriodProfileAdjuster(7.5),
            FlaggedRegionalDutyPeriodProfileAdjuster(flags, "regional-duty-period-profile-primary", 7.9),
            RegionalDutyPeriodProfileThresholdOffsetAdjuster(0.17),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 84890.2,
    )
}
