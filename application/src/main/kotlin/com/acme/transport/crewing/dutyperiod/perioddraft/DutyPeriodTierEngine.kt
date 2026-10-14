package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period tier calculation. */
interface DutyPeriodTierAdjuster {
    fun describe(): String
    fun adjust(item: DutyPeriodTier, value: Double): Double
}

data class DutyPeriodTierEtaMinutesOffsetAdjuster(var weight: Double) : DutyPeriodTierAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DutyPeriodTier, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class RoundingDutyPeriodTierAdjuster(var step: Double) : DutyPeriodTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodTier, value: Double): Double = roundTo(value, step)
}

data class FlaggedDutyPeriodTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDutyPeriodTierAdjuster(var minimum: Double) : DutyPeriodTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the duty period tier steps in order and keeps a trace of each. */
data class DutyPeriodTierEngine(
    var steps: MutableList<DutyPeriodTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodTierEngine(flags: Registry): DutyPeriodTierEngine {
    return DutyPeriodTierEngine(
        steps = mutableListOf<DutyPeriodTierAdjuster>(
            DutyPeriodTierEtaMinutesOffsetAdjuster(1.03),
            RoundingDutyPeriodTierAdjuster(1.0),
            FlaggedDutyPeriodTierAdjuster(flags, "duty-period-tier-scheduled", -3.5),
            MinimumDutyPeriodTierAdjuster(15.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 55526.3,
    )
}
