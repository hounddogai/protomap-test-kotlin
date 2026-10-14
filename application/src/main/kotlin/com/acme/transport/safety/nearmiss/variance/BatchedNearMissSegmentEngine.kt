package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched near miss segment calculation. */
interface BatchedNearMissSegmentFactor {
    fun describe(): String
    fun adjust(item: BatchedNearMissSegment, value: Double): Double
}

data class TieredBatchedNearMissSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNearMissSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNearMissSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleBatchedNearMissSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNearMissSegmentFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: BatchedNearMissSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedNearMissSegmentVersionOffsetAdjuster(var weight: Double) : BatchedNearMissSegmentFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: BatchedNearMissSegment, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingBatchedNearMissSegmentAdjuster(var step: Double) : BatchedNearMissSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNearMissSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the batched near miss segment steps in order and keeps a trace of each. */
data class BatchedNearMissSegmentEngine(
    var steps: MutableList<BatchedNearMissSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedNearMissSegment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedNearMissSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedNearMissSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedNearMissSegmentEngine(): BatchedNearMissSegmentEngine {
    return BatchedNearMissSegmentEngine(
        steps = mutableListOf<BatchedNearMissSegmentFactor>(
            TieredBatchedNearMissSegmentAdjuster(
                mutableListOf<Double>(677.0, 5432.0, 6055.0, 7395.0),
                mutableListOf<Double>(0.99, 0.95, 1.13, 1.06),
            ),
            LocaleBatchedNearMissSegmentAdjuster(
                mutableMapOf<String, Double>("retail" to 0.78, "north" to 0.77, "south" to 1.25),
            ),
            BatchedNearMissSegmentVersionOffsetAdjuster(1.0),
            RoundingBatchedNearMissSegmentAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 96056.5,
    )
}
