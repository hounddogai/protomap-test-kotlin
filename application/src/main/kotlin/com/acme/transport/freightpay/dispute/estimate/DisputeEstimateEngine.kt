package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the dispute estimate calculation. */
interface DisputeEstimateModifier {
    fun describe(): String
    fun adjust(item: DisputeEstimate, value: Double): Double
}

data class TieredDisputeEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DisputeEstimateBatchSizeOffsetAdjuster(var weight: Double) : DisputeEstimateModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: DisputeEstimate, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class FlaggedDisputeEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dispute estimate steps in order and keeps a trace of each. */
data class DisputeEstimateEngine(
    var steps: MutableList<DisputeEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DisputeEstimate): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDisputeEstimateEngine(flags: Registry): DisputeEstimateEngine {
    return DisputeEstimateEngine(
        steps = mutableListOf<DisputeEstimateModifier>(
            TieredDisputeEstimateAdjuster(
                mutableListOf<Double>(3826.0, 9685.0),
                mutableListOf<Double>(0.84, 1.16),
            ),
            DisputeEstimateBatchSizeOffsetAdjuster(1.9),
            FlaggedDisputeEstimateAdjuster(flags, "dispute-estimate-batched", -7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 22148.6,
    )
}
