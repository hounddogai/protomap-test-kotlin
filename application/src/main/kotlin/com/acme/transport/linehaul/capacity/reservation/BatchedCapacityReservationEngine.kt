package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched capacity reservation calculation. */
interface BatchedCapacityReservationAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCapacityReservation, value: Double): Double
}

data class RoundingBatchedCapacityReservationAdjuster(var step: Double) : BatchedCapacityReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCapacityReservation, value: Double): Double = roundTo(value, step)
}

data class BatchedCapacityReservationVersionOffsetAdjuster(
    var weight: Double,
) : BatchedCapacityReservationAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: BatchedCapacityReservation, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class MinimumBatchedCapacityReservationAdjuster(var minimum: Double) : BatchedCapacityReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCapacityReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedCapacityReservationAdjuster(var percent: Double) : BatchedCapacityReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCapacityReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched capacity reservation steps in order and keeps a trace of each. */
data class BatchedCapacityReservationEngine(
    var steps: MutableList<BatchedCapacityReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCapacityReservation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedCapacityReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedCapacityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCapacityReservationEngine(): BatchedCapacityReservationEngine {
    return BatchedCapacityReservationEngine(
        steps = mutableListOf<BatchedCapacityReservationAdjuster>(
            RoundingBatchedCapacityReservationAdjuster(0.25),
            BatchedCapacityReservationVersionOffsetAdjuster(1.53),
            MinimumBatchedCapacityReservationAdjuster(5.9),
            PercentBatchedCapacityReservationAdjuster(-9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 11457.0,
    )
}
