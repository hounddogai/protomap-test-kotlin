package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal node bundle calculation. */
interface SeasonalNodeBundleAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalNodeBundle, value: Double): Double
}

data class CapSeasonalNodeBundleAdjuster(var cap: Double) : SeasonalNodeBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalNodeBundle, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalNodeBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalNodeBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalNodeBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalNodeBundleAdjuster(var percent: Double) : SeasonalNodeBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalNodeBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalNodeBundleAdjuster(var step: Double) : SeasonalNodeBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalNodeBundle, value: Double): Double = roundTo(value, step)
}

data class SeasonalNodeBundlePalletsOffsetAdjuster(var weight: Double) : SeasonalNodeBundleAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: SeasonalNodeBundle, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class FlaggedSeasonalNodeBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalNodeBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalNodeBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal node bundle steps in order and keeps a trace of each. */
data class SeasonalNodeBundleEngine(
    var steps: MutableList<SeasonalNodeBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalNodeBundle): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalNodeBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalNodeBundleEngine(flags: Registry): SeasonalNodeBundleEngine {
    return SeasonalNodeBundleEngine(
        steps = mutableListOf<SeasonalNodeBundleAdjuster>(
            CapSeasonalNodeBundleAdjuster(29486.0),
            TieredSeasonalNodeBundleAdjuster(
                mutableListOf<Double>(270.0, 706.0, 3570.0),
                mutableListOf<Double>(1.18, 1.18, 0.91),
            ),
            PercentSeasonalNodeBundleAdjuster(-18.1),
            RoundingSeasonalNodeBundleAdjuster(1.0),
            SeasonalNodeBundlePalletsOffsetAdjuster(0.41),
            FlaggedSeasonalNodeBundleAdjuster(flags, "seasonal-node-bundle-adaptive", -5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 23031.2,
    )
}
