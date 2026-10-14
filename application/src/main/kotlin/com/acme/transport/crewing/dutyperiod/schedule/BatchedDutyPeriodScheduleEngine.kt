package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched duty period schedule calculation. */
interface BatchedDutyPeriodScheduleStep {
    fun describe(): String
    fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double
}

data class BatchedDutyPeriodScheduleScoreOffsetAdjuster(var weight: Double) : BatchedDutyPeriodScheduleStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double = value + item.score * weight
}

data class PercentBatchedDutyPeriodScheduleAdjuster(var percent: Double) : BatchedDutyPeriodScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedDutyPeriodScheduleAdjuster(var minimum: Double) : BatchedDutyPeriodScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedDutyPeriodScheduleAdjuster(var cap: Double) : BatchedDutyPeriodScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedDutyPeriodScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDutyPeriodScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedDutyPeriodScheduleAdjuster(var step: Double) : BatchedDutyPeriodScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDutyPeriodSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the batched duty period schedule steps in order and keeps a trace of each. */
data class BatchedDutyPeriodScheduleEngine(
    var steps: MutableList<BatchedDutyPeriodScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDutyPeriodSchedule): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDutyPeriodScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDutyPeriodSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDutyPeriodScheduleEngine(): BatchedDutyPeriodScheduleEngine {
    return BatchedDutyPeriodScheduleEngine(
        steps = mutableListOf<BatchedDutyPeriodScheduleStep>(
            BatchedDutyPeriodScheduleScoreOffsetAdjuster(0.05),
            PercentBatchedDutyPeriodScheduleAdjuster(1.5),
            MinimumBatchedDutyPeriodScheduleAdjuster(34.9),
            CapBatchedDutyPeriodScheduleAdjuster(25907.0),
            TieredBatchedDutyPeriodScheduleAdjuster(
                mutableListOf<Double>(7582.0, 8701.0),
                mutableListOf<Double>(1.11, 0.81),
            ),
            RoundingBatchedDutyPeriodScheduleAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 82691.4,
    )
}
