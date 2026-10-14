package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched fuel surcharge request calculation. */
interface BatchedFuelSurchargeRequestAdjuster {
    fun describe(): String
    fun adjust(item: BatchedFuelSurchargeRequest, value: Double): Double
}

data class ChannelBatchedFuelSurchargeRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedFuelSurchargeRequestAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedFuelSurchargeRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedFuelSurchargeRequestAdjuster(
    var percent: Double,
) : BatchedFuelSurchargeRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedFuelSurchargeRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedFuelSurchargeRequestAdjuster(
    var minimum: Double,
) : BatchedFuelSurchargeRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedFuelSurchargeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedFuelSurchargeRequestAdjuster(var cap: Double) : BatchedFuelSurchargeRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedFuelSurchargeRequest, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedFuelSurchargeRequestAdjuster(var step: Double) : BatchedFuelSurchargeRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedFuelSurchargeRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the batched fuel surcharge request steps in order and keeps a trace of each. */
data class BatchedFuelSurchargeRequestEngine(
    var steps: MutableList<BatchedFuelSurchargeRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedFuelSurchargeRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedFuelSurchargeRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedFuelSurchargeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedFuelSurchargeRequestEngine(): BatchedFuelSurchargeRequestEngine {
    return BatchedFuelSurchargeRequestEngine(
        steps = mutableListOf<BatchedFuelSurchargeRequestAdjuster>(
            ChannelBatchedFuelSurchargeRequestAdjuster(
                mutableMapOf<String, Double>("south" to 1.37, "online" to 1.03, "central" to 1.17),
            ),
            PercentBatchedFuelSurchargeRequestAdjuster(-9.9),
            MinimumBatchedFuelSurchargeRequestAdjuster(4.4),
            CapBatchedFuelSurchargeRequestAdjuster(49683.0),
            RoundingBatchedFuelSurchargeRequestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 53839.1,
    )
}
