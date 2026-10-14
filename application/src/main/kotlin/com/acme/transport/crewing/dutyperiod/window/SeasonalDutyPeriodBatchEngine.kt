package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal duty period batch calculation. */
interface SeasonalDutyPeriodBatchStep {
    fun describe(): String
    fun adjust(item: SeasonalDutyPeriodBatch, value: Double): Double
}

data class CapSeasonalDutyPeriodBatchAdjuster(var cap: Double) : SeasonalDutyPeriodBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDutyPeriodBatch, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalDutyPeriodBatchAdjuster(var step: Double) : SeasonalDutyPeriodBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDutyPeriodBatch, value: Double): Double = roundTo(value, step)
}

data class SeasonalDutyPeriodBatchQuantityOffsetAdjuster(var weight: Double) : SeasonalDutyPeriodBatchStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: SeasonalDutyPeriodBatch, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the seasonal duty period batch steps in order and keeps a trace of each. */
data class SeasonalDutyPeriodBatchEngine(
    var steps: MutableList<SeasonalDutyPeriodBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalDutyPeriodBatch): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDutyPeriodBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDutyPeriodBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalDutyPeriodBatchEngine(): SeasonalDutyPeriodBatchEngine {
    return SeasonalDutyPeriodBatchEngine(
        steps = mutableListOf<SeasonalDutyPeriodBatchStep>(
            CapSeasonalDutyPeriodBatchAdjuster(864.0),
            RoundingSeasonalDutyPeriodBatchAdjuster(0.25),
            SeasonalDutyPeriodBatchQuantityOffsetAdjuster(0.86),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 98390.5,
    )
}
