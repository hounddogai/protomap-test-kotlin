package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional fuel burn notice calculation. */
interface RegionalFuelBurnNoticeFactor {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnNotice, value: Double): Double
}

data class RegionalFuelBurnNoticeStartsAtOffsetAdjuster(var weight: Double) : RegionalFuelBurnNoticeFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RegionalFuelBurnNotice, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class FlaggedRegionalFuelBurnNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFuelBurnNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFuelBurnNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalFuelBurnNoticeAdjuster(var step: Double) : RegionalFuelBurnNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalFuelBurnNotice, value: Double): Double = roundTo(value, step)
}

data class RegionRegionalFuelBurnNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFuelBurnNoticeFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: RegionalFuelBurnNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalFuelBurnNoticeAdjuster(var cap: Double) : RegionalFuelBurnNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelBurnNotice, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalFuelBurnNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelBurnNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelBurnNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional fuel burn notice steps in order and keeps a trace of each. */
data class RegionalFuelBurnNoticeEngine(
    var steps: MutableList<RegionalFuelBurnNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalFuelBurnNotice): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelBurnNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelBurnNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnNoticeEngine(flags: Registry): RegionalFuelBurnNoticeEngine {
    return RegionalFuelBurnNoticeEngine(
        steps = mutableListOf<RegionalFuelBurnNoticeFactor>(
            RegionalFuelBurnNoticeStartsAtOffsetAdjuster(1.02),
            FlaggedRegionalFuelBurnNoticeAdjuster(flags, "regional-fuel-burn-notice-capped", 9.6),
            RoundingRegionalFuelBurnNoticeAdjuster(1.0),
            RegionRegionalFuelBurnNoticeAdjuster(mutableMapOf<String, Double>("retail" to 1.2, "north" to 1.32)),
            CapRegionalFuelBurnNoticeAdjuster(33765.0),
            TieredRegionalFuelBurnNoticeAdjuster(
                mutableListOf<Double>(4305.0, 5878.0),
                mutableListOf<Double>(1.02, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 43458.5,
    )
}
