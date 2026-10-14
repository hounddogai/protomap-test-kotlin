package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling fuel burn quota calculation. */
interface RollingFuelBurnQuotaFactor {
    fun describe(): String
    fun adjust(item: RollingFuelBurnQuota, value: Double): Double
}

data class MinimumRollingFuelBurnQuotaAdjuster(var minimum: Double) : RollingFuelBurnQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingFuelBurnQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingFuelBurnQuotaUpdatedAtOffsetAdjuster(var weight: Double) : RollingFuelBurnQuotaFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RollingFuelBurnQuota, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class CapRollingFuelBurnQuotaAdjuster(var cap: Double) : RollingFuelBurnQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelBurnQuota, value: Double): Double = minOf(value, cap)
}

data class TieredRollingFuelBurnQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFuelBurnQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFuelBurnQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling fuel burn quota steps in order and keeps a trace of each. */
data class RollingFuelBurnQuotaEngine(
    var steps: MutableList<RollingFuelBurnQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingFuelBurnQuota): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFuelBurnQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingFuelBurnQuotaEngine(): RollingFuelBurnQuotaEngine {
    return RollingFuelBurnQuotaEngine(
        steps = mutableListOf<RollingFuelBurnQuotaFactor>(
            MinimumRollingFuelBurnQuotaAdjuster(33.6),
            RollingFuelBurnQuotaUpdatedAtOffsetAdjuster(1.21),
            CapRollingFuelBurnQuotaAdjuster(44216.0),
            TieredRollingFuelBurnQuotaAdjuster(
                mutableListOf<Double>(1967.0, 6037.0),
                mutableListOf<Double>(0.98, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 52787.3,
    )
}
