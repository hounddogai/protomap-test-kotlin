package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched availability group calculation. */
interface BatchedAvailabilityGroupModifier {
    fun describe(): String
    fun adjust(item: BatchedAvailabilityGroup, value: Double): Double
}

data class BatchedAvailabilityGroupOdometerOffsetAdjuster(
    var weight: Double,
) : BatchedAvailabilityGroupModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: BatchedAvailabilityGroup, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class MinimumBatchedAvailabilityGroupAdjuster(var minimum: Double) : BatchedAvailabilityGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAvailabilityGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelBatchedAvailabilityGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedAvailabilityGroupModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedAvailabilityGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedAvailabilityGroupAdjuster(var step: Double) : BatchedAvailabilityGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedAvailabilityGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the batched availability group steps in order and keeps a trace of each. */
data class BatchedAvailabilityGroupEngine(
    var steps: MutableList<BatchedAvailabilityGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedAvailabilityGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAvailabilityGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAvailabilityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedAvailabilityGroupEngine(): BatchedAvailabilityGroupEngine {
    return BatchedAvailabilityGroupEngine(
        steps = mutableListOf<BatchedAvailabilityGroupModifier>(
            BatchedAvailabilityGroupOdometerOffsetAdjuster(1.16),
            MinimumBatchedAvailabilityGroupAdjuster(4.2),
            LabelBatchedAvailabilityGroupAdjuster(
                mutableMapOf<String, Double>("south" to 0.86, "central" to 0.74, "online" to 1.22),
            ),
            RoundingBatchedAvailabilityGroupAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 8803.4,
    )
}
