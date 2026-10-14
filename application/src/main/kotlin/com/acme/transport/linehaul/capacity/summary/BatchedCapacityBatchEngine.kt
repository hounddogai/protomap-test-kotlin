package com.acme.transport.linehaul.capacity.summary

/** One step of the batched capacity batch calculation. */
interface BatchedCapacityBatchFactor {
    fun describe(): String
    fun adjust(item: BatchedCapacityBatch, value: Double): Double
}

data class TieredBatchedCapacityBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCapacityBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCapacityBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedCapacityBatchFuelLitersOffsetAdjuster(var weight: Double) : BatchedCapacityBatchFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: BatchedCapacityBatch, value: Double): Double = value + item.fuelLiters * weight
}

data class CapBatchedCapacityBatchAdjuster(var cap: Double) : BatchedCapacityBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCapacityBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the batched capacity batch steps in order and keeps a trace of each. */
data class BatchedCapacityBatchEngine(
    var steps: MutableList<BatchedCapacityBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedCapacityBatch): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedCapacityBatchFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedCapacityBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedCapacityBatchEngine(): BatchedCapacityBatchEngine {
    return BatchedCapacityBatchEngine(
        steps = mutableListOf<BatchedCapacityBatchFactor>(
            TieredBatchedCapacityBatchAdjuster(
                mutableListOf<Double>(979.0, 8644.0, 9290.0),
                mutableListOf<Double>(1.11, 0.91, 0.99),
            ),
            BatchedCapacityBatchFuelLitersOffsetAdjuster(0.84),
            CapBatchedCapacityBatchAdjuster(12903.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 72023.5,
    )
}
