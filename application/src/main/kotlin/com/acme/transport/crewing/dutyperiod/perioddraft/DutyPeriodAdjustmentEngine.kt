package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the duty period adjustment calculation. */
interface DutyPeriodAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: DutyPeriodAdjustment, value: Double): Double
}

data class FlaggedDutyPeriodAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDutyPeriodAdjustmentAdjuster(var step: Double) : DutyPeriodAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodAdjustment, value: Double): Double = roundTo(value, step)
}

data class MinimumDutyPeriodAdjustmentAdjuster(var minimum: Double) : DutyPeriodAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDutyPeriodAdjustmentAdjuster(var percent: Double) : DutyPeriodAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDutyPeriodAdjustmentAdjuster(var cap: Double) : DutyPeriodAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the duty period adjustment steps in order and keeps a trace of each. */
data class DutyPeriodAdjustmentEngine(
    var steps: MutableList<DutyPeriodAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodAdjustment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodAdjustmentEngine(flags: Registry): DutyPeriodAdjustmentEngine {
    return DutyPeriodAdjustmentEngine(
        steps = mutableListOf<DutyPeriodAdjustmentAdjuster>(
            FlaggedDutyPeriodAdjustmentAdjuster(flags, "duty-period-adjustment-capped", 9.2),
            RoundingDutyPeriodAdjustmentAdjuster(0.5),
            MinimumDutyPeriodAdjustmentAdjuster(43.0),
            PercentDutyPeriodAdjustmentAdjuster(24.9),
            CapDutyPeriodAdjustmentAdjuster(22089.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 82938.5,
    )
}
