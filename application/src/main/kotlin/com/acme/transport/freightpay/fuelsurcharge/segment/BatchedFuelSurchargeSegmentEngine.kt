package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched fuel surcharge segment calculation. */
interface BatchedFuelSurchargeSegmentFactor {
    fun describe(): String
    fun adjust(item: BatchedFuelSurchargeSegment, value: Double): Double
}

data class TieredBatchedFuelSurchargeSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedFuelSurchargeSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedFuelSurchargeSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedFuelSurchargeSegmentPalletsOffsetAdjuster(
    var weight: Double,
) : BatchedFuelSurchargeSegmentFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: BatchedFuelSurchargeSegment, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class MinimumBatchedFuelSurchargeSegmentAdjuster(var minimum: Double) : BatchedFuelSurchargeSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedFuelSurchargeSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameBatchedFuelSurchargeSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedFuelSurchargeSegmentFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedFuelSurchargeSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedFuelSurchargeSegmentAdjuster(var cap: Double) : BatchedFuelSurchargeSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedFuelSurchargeSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the batched fuel surcharge segment steps in order and keeps a trace of each. */
data class BatchedFuelSurchargeSegmentEngine(
    var steps: MutableList<BatchedFuelSurchargeSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedFuelSurchargeSegment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedFuelSurchargeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedFuelSurchargeSegmentEngine(): BatchedFuelSurchargeSegmentEngine {
    return BatchedFuelSurchargeSegmentEngine(
        steps = mutableListOf<BatchedFuelSurchargeSegmentFactor>(
            TieredBatchedFuelSurchargeSegmentAdjuster(
                mutableListOf<Double>(1629.0, 3455.0, 9646.0),
                mutableListOf<Double>(0.97, 1.16, 1.18),
            ),
            BatchedFuelSurchargeSegmentPalletsOffsetAdjuster(1.83),
            MinimumBatchedFuelSurchargeSegmentAdjuster(40.6),
            NameBatchedFuelSurchargeSegmentAdjuster(mutableMapOf<String, Double>("retail" to 1.4)),
            CapBatchedFuelSurchargeSegmentAdjuster(46809.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 38189.6,
    )
}
