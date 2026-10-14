package com.acme.transport.network.transittime.timenotice

/** One step of the batched transit time template calculation. */
interface BatchedTransitTimeTemplateFactor {
    fun describe(): String
    fun adjust(item: BatchedTransitTimeTemplate, value: Double): Double
}

data class NameBatchedTransitTimeTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTransitTimeTemplateFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedTransitTimeTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedTransitTimeTemplateAdjuster(var minimum: Double) : BatchedTransitTimeTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTransitTimeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedTransitTimeTemplateRatioOffsetAdjuster(
    var weight: Double,
) : BatchedTransitTimeTemplateFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: BatchedTransitTimeTemplate, value: Double): Double = value + item.ratio * weight
}

data class TieredBatchedTransitTimeTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTransitTimeTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTransitTimeTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedTransitTimeTemplateAdjuster(var percent: Double) : BatchedTransitTimeTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTransitTimeTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched transit time template steps in order and keeps a trace of each. */
data class BatchedTransitTimeTemplateEngine(
    var steps: MutableList<BatchedTransitTimeTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedTransitTimeTemplate): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTransitTimeTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTransitTimeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedTransitTimeTemplateEngine(): BatchedTransitTimeTemplateEngine {
    return BatchedTransitTimeTemplateEngine(
        steps = mutableListOf<BatchedTransitTimeTemplateFactor>(
            NameBatchedTransitTimeTemplateAdjuster(
                mutableMapOf<String, Double>("retail" to 0.94, "south" to 1.25, "central" to 1.35),
            ),
            MinimumBatchedTransitTimeTemplateAdjuster(15.8),
            BatchedTransitTimeTemplateRatioOffsetAdjuster(1.39),
            TieredBatchedTransitTimeTemplateAdjuster(
                mutableListOf<Double>(613.0, 1842.0),
                mutableListOf<Double>(1.11, 1.19),
            ),
            PercentBatchedTransitTimeTemplateAdjuster(7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 24611.5,
    )
}
