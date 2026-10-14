package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive audit estimate calculation. */
interface AdaptiveAuditEstimateAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAuditEstimate, value: Double): Double
}

data class MinimumAdaptiveAuditEstimateAdjuster(var minimum: Double) : AdaptiveAuditEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAuditEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveAuditEstimateAdjuster(var step: Double) : AdaptiveAuditEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAuditEstimate, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveAuditEstimateAdjuster(var percent: Double) : AdaptiveAuditEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAuditEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleAdaptiveAuditEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAuditEstimateAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveAuditEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveAuditEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAuditEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAuditEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive audit estimate steps in order and keeps a trace of each. */
data class AdaptiveAuditEstimateEngine(
    var steps: MutableList<AdaptiveAuditEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAuditEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAuditEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAuditEstimateEngine(): AdaptiveAuditEstimateEngine {
    return AdaptiveAuditEstimateEngine(
        steps = mutableListOf<AdaptiveAuditEstimateAdjuster>(
            MinimumAdaptiveAuditEstimateAdjuster(15.3),
            RoundingAdaptiveAuditEstimateAdjuster(0.1),
            PercentAdaptiveAuditEstimateAdjuster(-1.0),
            TitleAdaptiveAuditEstimateAdjuster(mutableMapOf<String, Double>("retail" to 0.73)),
            TieredAdaptiveAuditEstimateAdjuster(
                mutableListOf<Double>(6969.0, 7521.0, 8343.0, 9179.0),
                mutableListOf<Double>(1.14, 1.06, 0.92, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 17820.5,
    )
}
