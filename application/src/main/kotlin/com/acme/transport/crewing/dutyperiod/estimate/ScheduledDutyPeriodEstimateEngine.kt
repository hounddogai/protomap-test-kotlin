package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled duty period estimate calculation. */
interface ScheduledDutyPeriodEstimateAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDutyPeriodEstimate, value: Double): Double
}

data class ScheduledDutyPeriodEstimateGraceDaysOffsetAdjuster(
    var weight: Double,
) : ScheduledDutyPeriodEstimateAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ScheduledDutyPeriodEstimate, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class CapScheduledDutyPeriodEstimateAdjuster(var cap: Double) : ScheduledDutyPeriodEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDutyPeriodEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledDutyPeriodEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDutyPeriodEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDutyPeriodEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledDutyPeriodEstimateAdjuster(var step: Double) : ScheduledDutyPeriodEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDutyPeriodEstimate, value: Double): Double = roundTo(value, step)
}

data class LocaleScheduledDutyPeriodEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDutyPeriodEstimateAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: ScheduledDutyPeriodEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled duty period estimate steps in order and keeps a trace of each. */
data class ScheduledDutyPeriodEstimateEngine(
    var steps: MutableList<ScheduledDutyPeriodEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDutyPeriodEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDutyPeriodEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDutyPeriodEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDutyPeriodEstimateEngine(): ScheduledDutyPeriodEstimateEngine {
    return ScheduledDutyPeriodEstimateEngine(
        steps = mutableListOf<ScheduledDutyPeriodEstimateAdjuster>(
            ScheduledDutyPeriodEstimateGraceDaysOffsetAdjuster(1.64),
            CapScheduledDutyPeriodEstimateAdjuster(13692.0),
            TieredScheduledDutyPeriodEstimateAdjuster(
                mutableListOf<Double>(1965.0, 5004.0, 7527.0, 8201.0),
                mutableListOf<Double>(1.18, 1.06, 1.04, 0.95),
            ),
            RoundingScheduledDutyPeriodEstimateAdjuster(0.25),
            LocaleScheduledDutyPeriodEstimateAdjuster(mutableMapOf<String, Double>("north" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 27738.0,
    )
}
