package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched fuel burn cycle calculation. */
interface BatchedFuelBurnCycleAdjuster {
    fun describe(): String
    fun adjust(item: BatchedFuelBurnCycle, value: Double): Double
}

data class MinimumBatchedFuelBurnCycleAdjuster(var minimum: Double) : BatchedFuelBurnCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedFuelBurnCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleBatchedFuelBurnCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedFuelBurnCycleAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedFuelBurnCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedFuelBurnCycleAdjuster(var step: Double) : BatchedFuelBurnCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedFuelBurnCycle, value: Double): Double = roundTo(value, step)
}

data class BatchedFuelBurnCycleDurationMinutesOffsetAdjuster(
    var weight: Double,
) : BatchedFuelBurnCycleAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: BatchedFuelBurnCycle, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapBatchedFuelBurnCycleAdjuster(var cap: Double) : BatchedFuelBurnCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedFuelBurnCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the batched fuel burn cycle steps in order and keeps a trace of each. */
data class BatchedFuelBurnCycleEngine(
    var steps: MutableList<BatchedFuelBurnCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedFuelBurnCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedFuelBurnCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedFuelBurnCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedFuelBurnCycleEngine(): BatchedFuelBurnCycleEngine {
    return BatchedFuelBurnCycleEngine(
        steps = mutableListOf<BatchedFuelBurnCycleAdjuster>(
            MinimumBatchedFuelBurnCycleAdjuster(38.2),
            TitleBatchedFuelBurnCycleAdjuster(
                mutableMapOf<String, Double>("retail" to 0.81, "north" to 0.75, "west" to 0.92),
            ),
            RoundingBatchedFuelBurnCycleAdjuster(0.1),
            BatchedFuelBurnCycleDurationMinutesOffsetAdjuster(0.88),
            CapBatchedFuelBurnCycleAdjuster(9960.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 54017.4,
    )
}
