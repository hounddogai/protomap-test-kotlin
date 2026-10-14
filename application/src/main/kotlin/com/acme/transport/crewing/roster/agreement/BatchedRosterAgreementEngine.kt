package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched roster agreement calculation. */
interface BatchedRosterAgreementFactor {
    fun describe(): String
    fun adjust(item: BatchedRosterAgreement, value: Double): Double
}

data class PercentBatchedRosterAgreementAdjuster(var percent: Double) : BatchedRosterAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRosterAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedRosterAgreementDiscountOffsetAdjuster(var weight: Double) : BatchedRosterAgreementFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedRosterAgreement, value: Double): Double = value + item.discount * weight
}

data class RoundingBatchedRosterAgreementAdjuster(var step: Double) : BatchedRosterAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRosterAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedRosterAgreementAdjuster(var minimum: Double) : BatchedRosterAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRosterAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleBatchedRosterAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRosterAgreementFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedRosterAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedRosterAgreementAdjuster(var cap: Double) : BatchedRosterAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRosterAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the batched roster agreement steps in order and keeps a trace of each. */
data class BatchedRosterAgreementEngine(
    var steps: MutableList<BatchedRosterAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRosterAgreement): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRosterAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRosterAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRosterAgreementEngine(): BatchedRosterAgreementEngine {
    return BatchedRosterAgreementEngine(
        steps = mutableListOf<BatchedRosterAgreementFactor>(
            PercentBatchedRosterAgreementAdjuster(10.6),
            BatchedRosterAgreementDiscountOffsetAdjuster(0.1),
            RoundingBatchedRosterAgreementAdjuster(0.5),
            MinimumBatchedRosterAgreementAdjuster(12.5),
            TitleBatchedRosterAgreementAdjuster(mutableMapOf<String, Double>("central" to 1.13)),
            CapBatchedRosterAgreementAdjuster(10805.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 36519.4,
    )
}
