package com.acme.transport.network.hub.legacyhubplan

/** One step of the batched hub exception calculation. */
interface BatchedHubExceptionAdjuster {
    fun describe(): String
    fun adjust(item: BatchedHubException, value: Double): Double
}

data class TieredBatchedHubExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedHubExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedHubException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryBatchedHubExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedHubExceptionAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: BatchedHubException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedHubExceptionCostOffsetAdjuster(var weight: Double) : BatchedHubExceptionAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: BatchedHubException, value: Double): Double = value + item.cost * weight
}

/** Runs the batched hub exception steps in order and keeps a trace of each. */
data class BatchedHubExceptionEngine(
    var steps: MutableList<BatchedHubExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedHubException): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedHubExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedHubException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedHubExceptionEngine(): BatchedHubExceptionEngine {
    return BatchedHubExceptionEngine(
        steps = mutableListOf<BatchedHubExceptionAdjuster>(
            TieredBatchedHubExceptionAdjuster(
                mutableListOf<Double>(2953.0, 3158.0, 3606.0),
                mutableListOf<Double>(1.02, 1.05, 1.11),
            ),
            CountryBatchedHubExceptionAdjuster(
                mutableMapOf<String, Double>("central" to 0.75, "south" to 0.79, "west" to 1.14),
            ),
            BatchedHubExceptionCostOffsetAdjuster(1.76),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 54700.0,
    )
}
