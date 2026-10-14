package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the capped cross dock profile calculation. */
interface CappedCrossDockProfileStep {
    fun describe(): String
    fun adjust(item: CappedCrossDockProfile, value: Double): Double
}

data class CapCappedCrossDockProfileAdjuster(var cap: Double) : CappedCrossDockProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCrossDockProfile, value: Double): Double = minOf(value, cap)
}

data class TieredCappedCrossDockProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCrossDockProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCrossDockProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedCrossDockProfileLimitOffsetAdjuster(var weight: Double) : CappedCrossDockProfileStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: CappedCrossDockProfile, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentCappedCrossDockProfileAdjuster(var percent: Double) : CappedCrossDockProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCrossDockProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedCrossDockProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCrossDockProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCrossDockProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped cross dock profile steps in order and keeps a trace of each. */
data class CappedCrossDockProfileEngine(
    var steps: MutableList<CappedCrossDockProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedCrossDockProfile): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCrossDockProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCrossDockProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedCrossDockProfileEngine(flags: Registry): CappedCrossDockProfileEngine {
    return CappedCrossDockProfileEngine(
        steps = mutableListOf<CappedCrossDockProfileStep>(
            CapCappedCrossDockProfileAdjuster(19565.0),
            TieredCappedCrossDockProfileAdjuster(
                mutableListOf<Double>(1083.0, 2983.0, 5145.0, 6295.0),
                mutableListOf<Double>(0.87, 1.11, 1.0, 0.82),
            ),
            CappedCrossDockProfileLimitOffsetAdjuster(1.74),
            PercentCappedCrossDockProfileAdjuster(-22.6),
            FlaggedCappedCrossDockProfileAdjuster(flags, "capped-cross-dock-profile-primary", 4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 23275.1,
    )
}
