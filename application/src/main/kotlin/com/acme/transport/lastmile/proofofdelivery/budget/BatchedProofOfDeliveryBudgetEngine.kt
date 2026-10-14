package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched proof of delivery budget calculation. */
interface BatchedProofOfDeliveryBudgetModifier {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double
}

data class CapBatchedProofOfDeliveryBudgetAdjuster(var cap: Double) : BatchedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedProofOfDeliveryBudgetAdjuster(
    var percent: Double,
) : BatchedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedProofOfDeliveryBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedProofOfDeliveryBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedProofOfDeliveryBudgetVersionOffsetAdjuster(
    var weight: Double,
) : BatchedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingBatchedProofOfDeliveryBudgetAdjuster(
    var step: Double,
) : BatchedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedProofOfDeliveryBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the batched proof of delivery budget steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryBudgetEngine(
    var steps: MutableList<BatchedProofOfDeliveryBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedProofOfDeliveryBudget): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedProofOfDeliveryBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryBudgetEngine(flags: Registry): BatchedProofOfDeliveryBudgetEngine {
    return BatchedProofOfDeliveryBudgetEngine(
        steps = mutableListOf<BatchedProofOfDeliveryBudgetModifier>(
            CapBatchedProofOfDeliveryBudgetAdjuster(32822.0),
            PercentBatchedProofOfDeliveryBudgetAdjuster(19.3),
            FlaggedBatchedProofOfDeliveryBudgetAdjuster(flags, "batched-proof-of-delivery-budget-regional", 6.3),
            TieredBatchedProofOfDeliveryBudgetAdjuster(
                mutableListOf<Double>(891.0, 2775.0),
                mutableListOf<Double>(0.88, 0.87),
            ),
            BatchedProofOfDeliveryBudgetVersionOffsetAdjuster(0.45),
            RoundingBatchedProofOfDeliveryBudgetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 97222.6,
    )
}
