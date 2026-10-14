package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback service area quota calculation. */
interface FallbackServiceAreaQuotaFactor {
    fun describe(): String
    fun adjust(item: FallbackServiceAreaQuota, value: Double): Double
}

data class PercentFallbackServiceAreaQuotaAdjuster(var percent: Double) : FallbackServiceAreaQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackServiceAreaQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackServiceAreaQuotaAdjuster(var minimum: Double) : FallbackServiceAreaQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackServiceAreaQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackServiceAreaQuotaLimitOffsetAdjuster(var weight: Double) : FallbackServiceAreaQuotaFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: FallbackServiceAreaQuota, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

/** Runs the fallback service area quota steps in order and keeps a trace of each. */
data class FallbackServiceAreaQuotaEngine(
    var steps: MutableList<FallbackServiceAreaQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackServiceAreaQuota): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackServiceAreaQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackServiceAreaQuotaEngine(): FallbackServiceAreaQuotaEngine {
    return FallbackServiceAreaQuotaEngine(
        steps = mutableListOf<FallbackServiceAreaQuotaFactor>(
            PercentFallbackServiceAreaQuotaAdjuster(-6.8),
            MinimumFallbackServiceAreaQuotaAdjuster(41.8),
            FallbackServiceAreaQuotaLimitOffsetAdjuster(0.03),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 97514.8,
    )
}
