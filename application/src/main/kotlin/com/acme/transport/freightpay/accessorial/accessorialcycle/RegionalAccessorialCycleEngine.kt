package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.flags.Registry

/** One step of the regional accessorial cycle calculation. */
interface RegionalAccessorialCycleAdjuster {
    fun describe(): String
    fun adjust(item: RegionalAccessorialCycle, value: Double): Double
}

data class MinimumRegionalAccessorialCycleAdjuster(var minimum: Double) : RegionalAccessorialCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalAccessorialCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalAccessorialCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAccessorialCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAccessorialCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalAccessorialCycleAdjuster(var cap: Double) : RegionalAccessorialCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalAccessorialCycle, value: Double): Double = minOf(value, cap)
}

data class LaneRegionalAccessorialCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalAccessorialCycleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RegionalAccessorialCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalAccessorialCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalAccessorialCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalAccessorialCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional accessorial cycle steps in order and keeps a trace of each. */
data class RegionalAccessorialCycleEngine(
    var steps: MutableList<RegionalAccessorialCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalAccessorialCycle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalAccessorialCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalAccessorialCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalAccessorialCycleEngine(flags: Registry): RegionalAccessorialCycleEngine {
    return RegionalAccessorialCycleEngine(
        steps = mutableListOf<RegionalAccessorialCycleAdjuster>(
            MinimumRegionalAccessorialCycleAdjuster(19.3),
            FlaggedRegionalAccessorialCycleAdjuster(flags, "regional-accessorial-cycle-fallback", 1.4),
            CapRegionalAccessorialCycleAdjuster(38245.0),
            LaneRegionalAccessorialCycleAdjuster(
                mutableMapOf<String, Double>("central" to 0.85, "north" to 1.12),
            ),
            TieredRegionalAccessorialCycleAdjuster(
                mutableListOf<Double>(357.0, 8612.0, 8854.0),
                mutableListOf<Double>(1.19, 1.08, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 53145.5,
    )
}
