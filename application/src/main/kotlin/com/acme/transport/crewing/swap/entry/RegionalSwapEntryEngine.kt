package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional swap entry calculation. */
interface RegionalSwapEntryAdjuster {
    fun describe(): String
    fun adjust(item: RegionalSwapEntry, value: Double): Double
}

data class UnitRegionalSwapEntryAdjuster(var factors: MutableMap<String, Double>) : RegionalSwapEntryAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalSwapEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalSwapEntryAdjuster(var minimum: Double) : RegionalSwapEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalSwapEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalSwapEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalSwapEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalSwapEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalSwapEntryAdjuster(var step: Double) : RegionalSwapEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalSwapEntry, value: Double): Double = roundTo(value, step)
}

data class RegionalSwapEntryMinimumOffsetAdjuster(var weight: Double) : RegionalSwapEntryAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: RegionalSwapEntry, value: Double): Double = value + item.minimum * weight
}

/** Runs the regional swap entry steps in order and keeps a trace of each. */
data class RegionalSwapEntryEngine(
    var steps: MutableList<RegionalSwapEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalSwapEntry): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalSwapEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalSwapEntryEngine(flags: Registry): RegionalSwapEntryEngine {
    return RegionalSwapEntryEngine(
        steps = mutableListOf<RegionalSwapEntryAdjuster>(
            UnitRegionalSwapEntryAdjuster(
                mutableMapOf<String, Double>("north" to 1.15, "online" to 1.18, "retail" to 0.87),
            ),
            MinimumRegionalSwapEntryAdjuster(40.1),
            FlaggedRegionalSwapEntryAdjuster(flags, "regional-swap-entry-scheduled", 3.4),
            RoundingRegionalSwapEntryAdjuster(0.5),
            RegionalSwapEntryMinimumOffsetAdjuster(0.78),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 57119.9,
    )
}
