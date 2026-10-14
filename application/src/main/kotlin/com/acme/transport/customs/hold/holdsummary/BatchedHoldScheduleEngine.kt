package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched hold schedule calculation. */
interface BatchedHoldScheduleAdjuster {
    fun describe(): String
    fun adjust(item: BatchedHoldSchedule, value: Double): Double
}

data class FlaggedBatchedHoldScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedHoldScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedHoldSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedHoldScheduleAdjuster(var minimum: Double) : BatchedHoldScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedHoldSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedHoldScheduleAdjuster(var cap: Double) : BatchedHoldScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedHoldSchedule, value: Double): Double = minOf(value, cap)
}

data class LabelBatchedHoldScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedHoldScheduleAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedHoldSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedHoldScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedHoldScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedHoldSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched hold schedule steps in order and keeps a trace of each. */
data class BatchedHoldScheduleEngine(
    var steps: MutableList<BatchedHoldScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedHoldSchedule): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedHoldSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedHoldScheduleEngine(flags: Registry): BatchedHoldScheduleEngine {
    return BatchedHoldScheduleEngine(
        steps = mutableListOf<BatchedHoldScheduleAdjuster>(
            FlaggedBatchedHoldScheduleAdjuster(flags, "batched-hold-schedule-batched", 6.5),
            MinimumBatchedHoldScheduleAdjuster(15.9),
            CapBatchedHoldScheduleAdjuster(1338.0),
            LabelBatchedHoldScheduleAdjuster(mutableMapOf<String, Double>("online" to 1.31)),
            TieredBatchedHoldScheduleAdjuster(
                mutableListOf<Double>(5769.0, 9577.0),
                mutableListOf<Double>(1.01, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 16515.7,
    )
}
