package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the pending fuel burn threshold calculation. */
interface PendingFuelBurnThresholdAdjuster {
    fun describe(): String
    fun adjust(item: PendingFuelBurnThreshold, value: Double): Double
}

data class PendingFuelBurnThresholdLoadKgOffsetAdjuster(var weight: Double) : PendingFuelBurnThresholdAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: PendingFuelBurnThreshold, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedPendingFuelBurnThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingFuelBurnThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingFuelBurnThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionPendingFuelBurnThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingFuelBurnThresholdAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingFuelBurnThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingFuelBurnThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingFuelBurnThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingFuelBurnThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending fuel burn threshold steps in order and keeps a trace of each. */
data class PendingFuelBurnThresholdEngine(
    var steps: MutableList<PendingFuelBurnThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingFuelBurnThreshold): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingFuelBurnThresholdAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingFuelBurnThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingFuelBurnThresholdEngine(flags: Registry): PendingFuelBurnThresholdEngine {
    return PendingFuelBurnThresholdEngine(
        steps = mutableListOf<PendingFuelBurnThresholdAdjuster>(
            PendingFuelBurnThresholdLoadKgOffsetAdjuster(1.33),
            FlaggedPendingFuelBurnThresholdAdjuster(flags, "pending-fuel-burn-threshold-default", 4.7),
            RegionPendingFuelBurnThresholdAdjuster(
                mutableMapOf<String, Double>("east" to 0.86, "central" to 0.76),
            ),
            TieredPendingFuelBurnThresholdAdjuster(
                mutableListOf<Double>(1946.0, 2209.0, 4738.0),
                mutableListOf<Double>(1.1, 1.05, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 79570.1,
    )
}
