package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.flags.Registry

/** One step of the regional zone quota calculation. */
interface RegionalZoneQuotaAdjuster {
    fun describe(): String
    fun adjust(item: RegionalZoneQuota, value: Double): Double
}

data class RegionalZoneQuotaDurationMinutesOffsetAdjuster(var weight: Double) : RegionalZoneQuotaAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RegionalZoneQuota, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedRegionalZoneQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalZoneQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalZoneQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalZoneQuotaAdjuster(var percent: Double) : RegionalZoneQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalZoneQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalZoneQuotaAdjuster(var minimum: Double) : RegionalZoneQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalZoneQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalZoneQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalZoneQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalZoneQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional zone quota steps in order and keeps a trace of each. */
data class RegionalZoneQuotaEngine(
    var steps: MutableList<RegionalZoneQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalZoneQuota): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalZoneQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalZoneQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalZoneQuotaEngine(flags: Registry): RegionalZoneQuotaEngine {
    return RegionalZoneQuotaEngine(
        steps = mutableListOf<RegionalZoneQuotaAdjuster>(
            RegionalZoneQuotaDurationMinutesOffsetAdjuster(1.48),
            FlaggedRegionalZoneQuotaAdjuster(flags, "regional-zone-quota-batched", 9.8),
            PercentRegionalZoneQuotaAdjuster(16.2),
            MinimumRegionalZoneQuotaAdjuster(5.9),
            TieredRegionalZoneQuotaAdjuster(
                mutableListOf<Double>(3656.0, 4683.0, 5533.0, 8918.0),
                mutableListOf<Double>(0.99, 1.0, 0.83, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 64775.0,
    )
}
