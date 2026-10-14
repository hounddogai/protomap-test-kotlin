package com.acme.transport.network.hub.primaryhubsummary

/** One step of the batched hub tier calculation. */
interface BatchedHubTierStep {
    fun describe(): String
    fun adjust(item: BatchedHubTier, value: Double): Double
}

data class TieredBatchedHubTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedHubTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedHubTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedHubTierAdjuster(var percent: Double) : BatchedHubTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedHubTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleBatchedHubTierAdjuster(var factors: MutableMap<String, Double>) : BatchedHubTierStep {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedHubTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedHubTierSequenceOffsetAdjuster(var weight: Double) : BatchedHubTierStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: BatchedHubTier, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the batched hub tier steps in order and keeps a trace of each. */
data class BatchedHubTierEngine(
    var steps: MutableList<BatchedHubTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedHubTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedHubTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedHubTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedHubTierEngine(): BatchedHubTierEngine {
    return BatchedHubTierEngine(
        steps = mutableListOf<BatchedHubTierStep>(
            TieredBatchedHubTierAdjuster(
                mutableListOf<Double>(5063.0, 6094.0),
                mutableListOf<Double>(1.17, 0.88),
            ),
            PercentBatchedHubTierAdjuster(3.9),
            TitleBatchedHubTierAdjuster(
                mutableMapOf<String, Double>("south" to 1.32, "west" to 0.91, "east" to 0.99),
            ),
            BatchedHubTierSequenceOffsetAdjuster(0.17),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 2258.5,
    )
}
