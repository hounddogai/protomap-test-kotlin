package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional lease return entry calculation. */
interface RegionalLeaseReturnEntryModifier {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double
}

data class RegionalLeaseReturnEntryRatioOffsetAdjuster(var weight: Double) : RegionalLeaseReturnEntryModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double = value + item.ratio * weight
}

data class CapRegionalLeaseReturnEntryAdjuster(var cap: Double) : RegionalLeaseReturnEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalLeaseReturnEntryAdjuster(var percent: Double) : RegionalLeaseReturnEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameRegionalLeaseReturnEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalLeaseReturnEntryModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalLeaseReturnEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalLeaseReturnEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalLeaseReturnEntryAdjuster(var minimum: Double) : RegionalLeaseReturnEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional lease return entry steps in order and keeps a trace of each. */
data class RegionalLeaseReturnEntryEngine(
    var steps: MutableList<RegionalLeaseReturnEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalLeaseReturnEntry): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalLeaseReturnEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnEntryEngine(): RegionalLeaseReturnEntryEngine {
    return RegionalLeaseReturnEntryEngine(
        steps = mutableListOf<RegionalLeaseReturnEntryModifier>(
            RegionalLeaseReturnEntryRatioOffsetAdjuster(1.61),
            CapRegionalLeaseReturnEntryAdjuster(10005.0),
            PercentRegionalLeaseReturnEntryAdjuster(14.6),
            NameRegionalLeaseReturnEntryAdjuster(mutableMapOf<String, Double>("east" to 1.38, "north" to 1.34)),
            TieredRegionalLeaseReturnEntryAdjuster(
                mutableListOf<Double>(628.0, 8866.0),
                mutableListOf<Double>(1.03, 1.08),
            ),
            MinimumRegionalLeaseReturnEntryAdjuster(25.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 52666.6,
    )
}
