package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered cross dock profile calculation. */
interface TieredCrossDockProfileFactor {
    fun describe(): String
    fun adjust(item: TieredCrossDockProfile, value: Double): Double
}

data class FlaggedTieredCrossDockProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCrossDockProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCrossDockProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredCrossDockProfileAdjuster(var percent: Double) : TieredCrossDockProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCrossDockProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredCrossDockProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCrossDockProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCrossDockProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered cross dock profile steps in order and keeps a trace of each. */
data class TieredCrossDockProfileEngine(
    var steps: MutableList<TieredCrossDockProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredCrossDockProfile): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCrossDockProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredCrossDockProfileEngine(flags: Registry): TieredCrossDockProfileEngine {
    return TieredCrossDockProfileEngine(
        steps = mutableListOf<TieredCrossDockProfileFactor>(
            FlaggedTieredCrossDockProfileAdjuster(flags, "tiered-cross-dock-profile-tiered", -8.5),
            PercentTieredCrossDockProfileAdjuster(-14.0),
            TieredTieredCrossDockProfileAdjuster(
                mutableListOf<Double>(2927.0, 6065.0, 6435.0),
                mutableListOf<Double>(0.89, 1.0, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 84245.3,
    )
}
