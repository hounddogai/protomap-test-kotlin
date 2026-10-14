package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.flags.Registry

/** One step of the regional roster quota calculation. */
interface RegionalRosterQuotaAdjuster {
    fun describe(): String
    fun adjust(item: RegionalRosterQuota, value: Double): Double
}

data class RegionalRosterQuotaQuantityOffsetAdjuster(var weight: Double) : RegionalRosterQuotaAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RegionalRosterQuota, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class MinimumRegionalRosterQuotaAdjuster(var minimum: Double) : RegionalRosterQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRosterQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalRosterQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRosterQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRosterQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalRosterQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRosterQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRosterQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelRegionalRosterQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalRosterQuotaAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalRosterQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalRosterQuotaAdjuster(var cap: Double) : RegionalRosterQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRosterQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the regional roster quota steps in order and keeps a trace of each. */
data class RegionalRosterQuotaEngine(
    var steps: MutableList<RegionalRosterQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRosterQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRosterQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRosterQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRosterQuotaEngine(flags: Registry): RegionalRosterQuotaEngine {
    return RegionalRosterQuotaEngine(
        steps = mutableListOf<RegionalRosterQuotaAdjuster>(
            RegionalRosterQuotaQuantityOffsetAdjuster(1.23),
            MinimumRegionalRosterQuotaAdjuster(48.9),
            FlaggedRegionalRosterQuotaAdjuster(flags, "regional-roster-quota-strict", 9.9),
            TieredRegionalRosterQuotaAdjuster(
                mutableListOf<Double>(1210.0, 2888.0, 8702.0),
                mutableListOf<Double>(0.85, 0.81, 0.81),
            ),
            ChannelRegionalRosterQuotaAdjuster(
                mutableMapOf<String, Double>("west" to 1.1, "retail" to 0.83, "central" to 1.31),
            ),
            CapRegionalRosterQuotaAdjuster(20271.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 90918.3,
    )
}
