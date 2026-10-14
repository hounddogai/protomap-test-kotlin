package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal parcel quota calculation. */
interface SeasonalParcelQuotaStep {
    fun describe(): String
    fun adjust(item: SeasonalParcelQuota, value: Double): Double
}

data class FlaggedSeasonalParcelQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalParcelQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalParcelQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalParcelQuotaAdjuster(var minimum: Double) : SeasonalParcelQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalParcelQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalParcelQuotaCapacityOffsetAdjuster(var weight: Double) : SeasonalParcelQuotaStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: SeasonalParcelQuota, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class CapSeasonalParcelQuotaAdjuster(var cap: Double) : SeasonalParcelQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalParcelQuota, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalParcelQuotaAdjuster(var percent: Double) : SeasonalParcelQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalParcelQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal parcel quota steps in order and keeps a trace of each. */
data class SeasonalParcelQuotaEngine(
    var steps: MutableList<SeasonalParcelQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalParcelQuota): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalParcelQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalParcelQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalParcelQuotaEngine(flags: Registry): SeasonalParcelQuotaEngine {
    return SeasonalParcelQuotaEngine(
        steps = mutableListOf<SeasonalParcelQuotaStep>(
            FlaggedSeasonalParcelQuotaAdjuster(flags, "seasonal-parcel-quota-regional", -4.8),
            MinimumSeasonalParcelQuotaAdjuster(42.0),
            SeasonalParcelQuotaCapacityOffsetAdjuster(0.95),
            CapSeasonalParcelQuotaAdjuster(6120.0),
            PercentSeasonalParcelQuotaAdjuster(14.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 57189.3,
    )
}
