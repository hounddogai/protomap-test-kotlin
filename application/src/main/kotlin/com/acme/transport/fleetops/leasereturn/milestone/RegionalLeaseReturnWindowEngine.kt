package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional lease return window calculation. */
interface RegionalLeaseReturnWindowModifier {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double
}

data class TieredRegionalLeaseReturnWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalLeaseReturnWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalLeaseReturnWindowScoreOffsetAdjuster(
    var weight: Double,
) : RegionalLeaseReturnWindowModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double = value + item.score * weight
}

data class CapRegionalLeaseReturnWindowAdjuster(var cap: Double) : RegionalLeaseReturnWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double = minOf(value, cap)
}

data class NameRegionalLeaseReturnWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalLeaseReturnWindowModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalLeaseReturnWindowAdjuster(var minimum: Double) : RegionalLeaseReturnWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalLeaseReturnWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalLeaseReturnWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalLeaseReturnWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional lease return window steps in order and keeps a trace of each. */
data class RegionalLeaseReturnWindowEngine(
    var steps: MutableList<RegionalLeaseReturnWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalLeaseReturnWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalLeaseReturnWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnWindowEngine(flags: Registry): RegionalLeaseReturnWindowEngine {
    return RegionalLeaseReturnWindowEngine(
        steps = mutableListOf<RegionalLeaseReturnWindowModifier>(
            TieredRegionalLeaseReturnWindowAdjuster(
                mutableListOf<Double>(5484.0, 5506.0, 6622.0, 6686.0),
                mutableListOf<Double>(1.14, 1.14, 1.15, 1.02),
            ),
            RegionalLeaseReturnWindowScoreOffsetAdjuster(0.11),
            CapRegionalLeaseReturnWindowAdjuster(15937.0),
            NameRegionalLeaseReturnWindowAdjuster(
                mutableMapOf<String, Double>("online" to 1.22, "retail" to 0.99, "south" to 1.13),
            ),
            MinimumRegionalLeaseReturnWindowAdjuster(42.8),
            FlaggedRegionalLeaseReturnWindowAdjuster(flags, "regional-lease-return-window-batched", 6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 17313.8,
    )
}
