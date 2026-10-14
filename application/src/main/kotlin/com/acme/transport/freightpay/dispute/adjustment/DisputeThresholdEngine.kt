package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the dispute threshold calculation. */
interface DisputeThresholdStep {
    fun describe(): String
    fun adjust(item: DisputeThreshold, value: Double): Double
}

data class TieredDisputeThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DisputeThresholdCreatedAtOffsetAdjuster(var weight: Double) : DisputeThresholdStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: DisputeThreshold, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class DepotDisputeThresholdAdjuster(var factors: MutableMap<String, Double>) : DisputeThresholdStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DisputeThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDisputeThresholdAdjuster(var percent: Double) : DisputeThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDisputeThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dispute threshold steps in order and keeps a trace of each. */
data class DisputeThresholdEngine(
    var steps: MutableList<DisputeThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DisputeThreshold): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDisputeThresholdEngine(flags: Registry): DisputeThresholdEngine {
    return DisputeThresholdEngine(
        steps = mutableListOf<DisputeThresholdStep>(
            TieredDisputeThresholdAdjuster(
                mutableListOf<Double>(995.0, 1657.0),
                mutableListOf<Double>(1.06, 0.87),
            ),
            DisputeThresholdCreatedAtOffsetAdjuster(0.1),
            DepotDisputeThresholdAdjuster(mutableMapOf<String, Double>("online" to 1.35)),
            PercentDisputeThresholdAdjuster(-24.9),
            FlaggedDisputeThresholdAdjuster(flags, "dispute-threshold-adaptive", -6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 92416.7,
    )
}
