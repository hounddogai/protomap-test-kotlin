package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial estimate calculation. */
interface AccessorialEstimateStep {
    fun describe(): String
    fun adjust(item: AccessorialEstimate, value: Double): Double
}

data class PercentAccessorialEstimateAdjuster(var percent: Double) : AccessorialEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AccessorialEstimateCostOffsetAdjuster(var weight: Double) : AccessorialEstimateStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: AccessorialEstimate, value: Double): Double = value + item.cost * weight
}

data class TieredAccessorialEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAccessorialEstimateAdjuster(var step: Double) : AccessorialEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialEstimate, value: Double): Double = roundTo(value, step)
}

data class ChannelAccessorialEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialEstimateStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: AccessorialEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the accessorial estimate steps in order and keeps a trace of each. */
data class AccessorialEstimateEngine(
    var steps: MutableList<AccessorialEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialEstimate): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialEstimateEngine(): AccessorialEstimateEngine {
    return AccessorialEstimateEngine(
        steps = mutableListOf<AccessorialEstimateStep>(
            PercentAccessorialEstimateAdjuster(-18.0),
            AccessorialEstimateCostOffsetAdjuster(1.11),
            TieredAccessorialEstimateAdjuster(
                mutableListOf<Double>(1542.0, 3134.0, 7566.0),
                mutableListOf<Double>(1.0, 1.05, 1.12),
            ),
            RoundingAccessorialEstimateAdjuster(0.1),
            ChannelAccessorialEstimateAdjuster(mutableMapOf<String, Double>("north" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 25197.7,
    )
}
