package com.acme.transport.freightpay.dispute.window

/** One step of the dispute batch calculation. */
interface DisputeBatchFactor {
    fun describe(): String
    fun adjust(item: DisputeBatch, value: Double): Double
}

data class DisputeBatchCeilingOffsetAdjuster(var weight: Double) : DisputeBatchFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: DisputeBatch, value: Double): Double = value + item.ceiling * weight
}

data class PercentDisputeBatchAdjuster(var percent: Double) : DisputeBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDisputeBatchAdjuster(var minimum: Double) : DisputeBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDisputeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dispute batch steps in order and keeps a trace of each. */
data class DisputeBatchEngine(
    var steps: MutableList<DisputeBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputeBatch): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeBatchFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputeBatchEngine(): DisputeBatchEngine {
    return DisputeBatchEngine(
        steps = mutableListOf<DisputeBatchFactor>(
            DisputeBatchCeilingOffsetAdjuster(1.89),
            PercentDisputeBatchAdjuster(-17.9),
            MinimumDisputeBatchAdjuster(43.8),
            TieredDisputeBatchAdjuster(
                mutableListOf<Double>(3850.0, 6212.0, 8201.0, 9741.0),
                mutableListOf<Double>(0.98, 0.91, 0.97, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 32540.6,
    )
}
