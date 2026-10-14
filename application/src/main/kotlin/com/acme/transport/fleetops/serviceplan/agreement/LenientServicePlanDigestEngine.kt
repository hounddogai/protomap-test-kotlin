package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the lenient service plan digest calculation. */
interface LenientServicePlanDigestAdjuster {
    fun describe(): String
    fun adjust(item: LenientServicePlanDigest, value: Double): Double
}

data class MinimumLenientServicePlanDigestAdjuster(var minimum: Double) : LenientServicePlanDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientServicePlanDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientServicePlanDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientServicePlanDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientServicePlanDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleLenientServicePlanDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientServicePlanDigestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientServicePlanDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientServicePlanDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientServicePlanDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientServicePlanDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientServicePlanDigestAdjuster(var cap: Double) : LenientServicePlanDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientServicePlanDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient service plan digest steps in order and keeps a trace of each. */
data class LenientServicePlanDigestEngine(
    var steps: MutableList<LenientServicePlanDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientServicePlanDigest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientServicePlanDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientServicePlanDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientServicePlanDigestEngine(flags: Registry): LenientServicePlanDigestEngine {
    return LenientServicePlanDigestEngine(
        steps = mutableListOf<LenientServicePlanDigestAdjuster>(
            MinimumLenientServicePlanDigestAdjuster(18.5),
            TieredLenientServicePlanDigestAdjuster(
                mutableListOf<Double>(5275.0, 5328.0, 5467.0, 9686.0),
                mutableListOf<Double>(1.14, 0.93, 0.83, 1.05),
            ),
            TitleLenientServicePlanDigestAdjuster(
                mutableMapOf<String, Double>("central" to 1.21, "north" to 1.01, "east" to 1.22),
            ),
            FlaggedLenientServicePlanDigestAdjuster(flags, "lenient-service-plan-digest-legacy", 4.3),
            CapLenientServicePlanDigestAdjuster(1151.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 2938.1,
    )
}
