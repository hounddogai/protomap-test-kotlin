package com.acme.transport.linehaul.trailer.trailerrule

/** One step of the batched trailer rule calculation. */
interface BatchedTrailerRuleStep {
    fun describe(): String
    fun adjust(item: BatchedTrailerRule, value: Double): Double
}

data class CapBatchedTrailerRuleAdjuster(var cap: Double) : BatchedTrailerRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTrailerRule, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedTrailerRuleAdjuster(var minimum: Double) : BatchedTrailerRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTrailerRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedTrailerRuleAdjuster(var percent: Double) : BatchedTrailerRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTrailerRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched trailer rule steps in order and keeps a trace of each. */
data class BatchedTrailerRuleEngine(
    var steps: MutableList<BatchedTrailerRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTrailerRule): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTrailerRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTrailerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTrailerRuleEngine(): BatchedTrailerRuleEngine {
    return BatchedTrailerRuleEngine(
        steps = mutableListOf<BatchedTrailerRuleStep>(
            CapBatchedTrailerRuleAdjuster(26851.0),
            MinimumBatchedTrailerRuleAdjuster(6.3),
            PercentBatchedTrailerRuleAdjuster(-20.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 19092.2,
    )
}
