package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling utilization quota calculation. */
interface RollingUtilizationQuotaStep {
    fun describe(): String
    fun adjust(item: RollingUtilizationQuota, value: Double): Double
}

data class TieredRollingUtilizationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingUtilizationQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingUtilizationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingUtilizationQuotaFuelLitersOffsetAdjuster(var weight: Double) : RollingUtilizationQuotaStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RollingUtilizationQuota, value: Double): Double = value + item.fuelLiters * weight
}

data class RoundingRollingUtilizationQuotaAdjuster(var step: Double) : RollingUtilizationQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingUtilizationQuota, value: Double): Double = roundTo(value, step)
}

data class CapRollingUtilizationQuotaAdjuster(var cap: Double) : RollingUtilizationQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingUtilizationQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling utilization quota steps in order and keeps a trace of each. */
data class RollingUtilizationQuotaEngine(
    var steps: MutableList<RollingUtilizationQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingUtilizationQuota): Double {
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

    fun evaluateAll(items: MutableList<RollingUtilizationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingUtilizationQuotaEngine(): RollingUtilizationQuotaEngine {
    return RollingUtilizationQuotaEngine(
        steps = mutableListOf<RollingUtilizationQuotaStep>(
            TieredRollingUtilizationQuotaAdjuster(
                mutableListOf<Double>(8321.0, 8786.0, 9387.0),
                mutableListOf<Double>(1.02, 1.13, 0.95),
            ),
            RollingUtilizationQuotaFuelLitersOffsetAdjuster(1.75),
            RoundingRollingUtilizationQuotaAdjuster(0.5),
            CapRollingUtilizationQuotaAdjuster(36574.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 11193.6,
    )
}
