package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling ev charge quota calculation. */
interface RollingEvChargeQuotaAdjuster {
    fun describe(): String
    fun adjust(item: RollingEvChargeQuota, value: Double): Double
}

data class MinimumRollingEvChargeQuotaAdjuster(var minimum: Double) : RollingEvChargeQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingEvChargeQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingEvChargeQuotaAdjuster(var percent: Double) : RollingEvChargeQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingEvChargeQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingEvChargeQuotaAdjuster(var cap: Double) : RollingEvChargeQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingEvChargeQuota, value: Double): Double = minOf(value, cap)
}

data class TieredRollingEvChargeQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingEvChargeQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingEvChargeQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling ev charge quota steps in order and keeps a trace of each. */
data class RollingEvChargeQuotaEngine(
    var steps: MutableList<RollingEvChargeQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingEvChargeQuota): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingEvChargeQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingEvChargeQuotaEngine(): RollingEvChargeQuotaEngine {
    return RollingEvChargeQuotaEngine(
        steps = mutableListOf<RollingEvChargeQuotaAdjuster>(
            MinimumRollingEvChargeQuotaAdjuster(37.8),
            PercentRollingEvChargeQuotaAdjuster(22.4),
            CapRollingEvChargeQuotaAdjuster(47764.0),
            TieredRollingEvChargeQuotaAdjuster(
                mutableListOf<Double>(953.0, 2273.0, 6598.0, 8624.0),
                mutableListOf<Double>(1.05, 1.0, 0.91, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 1376.2,
    )
}
