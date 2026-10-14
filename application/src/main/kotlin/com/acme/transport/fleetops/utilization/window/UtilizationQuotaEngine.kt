package com.acme.transport.fleetops.utilization.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization quota calculation. */
interface UtilizationQuotaFactor {
    fun describe(): String
    fun adjust(item: UtilizationQuota, value: Double): Double
}

data class TieredUtilizationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UtilizationQuotaDurationMinutesOffsetAdjuster(var weight: Double) : UtilizationQuotaFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: UtilizationQuota, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class MinimumUtilizationQuotaAdjuster(var minimum: Double) : UtilizationQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the utilization quota steps in order and keeps a trace of each. */
data class UtilizationQuotaEngine(
    var steps: MutableList<UtilizationQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: UtilizationQuota): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultUtilizationQuotaEngine(): UtilizationQuotaEngine {
    return UtilizationQuotaEngine(
        steps = mutableListOf<UtilizationQuotaFactor>(
            TieredUtilizationQuotaAdjuster(
                mutableListOf<Double>(3042.0, 4426.0),
                mutableListOf<Double>(0.82, 1.12),
            ),
            UtilizationQuotaDurationMinutesOffsetAdjuster(1.46),
            MinimumUtilizationQuotaAdjuster(18.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 44479.3,
    )
}
