package com.acme.transport.network.hub.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered hub baseline calculation. */
interface TieredHubBaselineAdjuster {
    fun describe(): String
    fun adjust(item: TieredHubBaseline, value: Double): Double
}

data class FlaggedTieredHubBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredHubBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredHubBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredHubBaselineAdjuster(var cap: Double) : TieredHubBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredHubBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredTieredHubBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredHubBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredHubBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredHubBaselineAdjuster(var percent: Double) : TieredHubBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredHubBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameTieredHubBaselineAdjuster(var factors: MutableMap<String, Double>) : TieredHubBaselineAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredHubBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered hub baseline steps in order and keeps a trace of each. */
data class TieredHubBaselineEngine(
    var steps: MutableList<TieredHubBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredHubBaseline): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredHubBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredHubBaselineEngine(flags: Registry): TieredHubBaselineEngine {
    return TieredHubBaselineEngine(
        steps = mutableListOf<TieredHubBaselineAdjuster>(
            FlaggedTieredHubBaselineAdjuster(flags, "tiered-hub-baseline-weighted", -8.8),
            CapTieredHubBaselineAdjuster(7829.0),
            TieredTieredHubBaselineAdjuster(
                mutableListOf<Double>(2960.0, 6648.0, 8152.0),
                mutableListOf<Double>(1.0, 1.1, 1.13),
            ),
            PercentTieredHubBaselineAdjuster(7.8),
            NameTieredHubBaselineAdjuster(
                mutableMapOf<String, Double>("east" to 1.17, "retail" to 0.94, "south" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 23644.0,
    )
}
