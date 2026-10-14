package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched ev charge schedule calculation. */
interface BatchedEvChargeScheduleStep {
    fun describe(): String
    fun adjust(item: BatchedEvChargeSchedule, value: Double): Double
}

data class RoundingBatchedEvChargeScheduleAdjuster(var step: Double) : BatchedEvChargeScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedEvChargeSchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedEvChargeScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedEvChargeScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedEvChargeSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedEvChargeScheduleAdjuster(var percent: Double) : BatchedEvChargeScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedEvChargeSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched ev charge schedule steps in order and keeps a trace of each. */
data class BatchedEvChargeScheduleEngine(
    var steps: MutableList<BatchedEvChargeScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedEvChargeSchedule): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedEvChargeSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedEvChargeScheduleEngine(flags: Registry): BatchedEvChargeScheduleEngine {
    return BatchedEvChargeScheduleEngine(
        steps = mutableListOf<BatchedEvChargeScheduleStep>(
            RoundingBatchedEvChargeScheduleAdjuster(0.05),
            FlaggedBatchedEvChargeScheduleAdjuster(flags, "batched-ev-charge-schedule-pending", 1.6),
            PercentBatchedEvChargeScheduleAdjuster(7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 15023.4,
    )
}
