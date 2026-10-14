package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched dispute reservation calculation. */
interface BatchedDisputeReservationModifier {
    fun describe(): String
    fun adjust(item: BatchedDisputeReservation, value: Double): Double
}

data class CapBatchedDisputeReservationAdjuster(var cap: Double) : BatchedDisputeReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDisputeReservation, value: Double): Double = minOf(value, cap)
}

data class LocaleBatchedDisputeReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDisputeReservationModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: BatchedDisputeReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedDisputeReservationAdjuster(var percent: Double) : BatchedDisputeReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDisputeReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedDisputeReservationLimitOffsetAdjuster(
    var weight: Double,
) : BatchedDisputeReservationModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: BatchedDisputeReservation, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

/** Runs the batched dispute reservation steps in order and keeps a trace of each. */
data class BatchedDisputeReservationEngine(
    var steps: MutableList<BatchedDisputeReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDisputeReservation): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDisputeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDisputeReservationEngine(): BatchedDisputeReservationEngine {
    return BatchedDisputeReservationEngine(
        steps = mutableListOf<BatchedDisputeReservationModifier>(
            CapBatchedDisputeReservationAdjuster(20648.0),
            LocaleBatchedDisputeReservationAdjuster(mutableMapOf<String, Double>("east" to 1.25)),
            PercentBatchedDisputeReservationAdjuster(3.3),
            BatchedDisputeReservationLimitOffsetAdjuster(0.08),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 81972.9,
    )
}
