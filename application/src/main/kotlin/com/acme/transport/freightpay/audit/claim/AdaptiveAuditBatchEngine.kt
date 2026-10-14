package com.acme.transport.freightpay.audit.claim

/** One step of the adaptive audit batch calculation. */
interface AdaptiveAuditBatchAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAuditBatch, value: Double): Double
}

data class ChannelAdaptiveAuditBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAuditBatchAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveAuditBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveAuditBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAuditBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAuditBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveAuditBatchAdjuster(var minimum: Double) : AdaptiveAuditBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAuditBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveAuditBatchAdjuster(var percent: Double) : AdaptiveAuditBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAuditBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveAuditBatchPalletsOffsetAdjuster(var weight: Double) : AdaptiveAuditBatchAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: AdaptiveAuditBatch, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the adaptive audit batch steps in order and keeps a trace of each. */
data class AdaptiveAuditBatchEngine(
    var steps: MutableList<AdaptiveAuditBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveAuditBatch): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAuditBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAuditBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditBatchEngine(): AdaptiveAuditBatchEngine {
    return AdaptiveAuditBatchEngine(
        steps = mutableListOf<AdaptiveAuditBatchAdjuster>(
            ChannelAdaptiveAuditBatchAdjuster(
                mutableMapOf<String, Double>("retail" to 0.76, "online" to 1.09, "east" to 0.91),
            ),
            TieredAdaptiveAuditBatchAdjuster(
                mutableListOf<Double>(3790.0, 4969.0, 6484.0),
                mutableListOf<Double>(0.86, 0.97, 0.89),
            ),
            MinimumAdaptiveAuditBatchAdjuster(9.3),
            PercentAdaptiveAuditBatchAdjuster(24.9),
            AdaptiveAuditBatchPalletsOffsetAdjuster(2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 21238.4,
    )
}
