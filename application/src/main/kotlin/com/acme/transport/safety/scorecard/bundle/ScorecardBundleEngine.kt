package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scorecard bundle calculation. */
interface ScorecardBundleFactor {
    fun describe(): String
    fun adjust(item: ScorecardBundle, value: Double): Double
}

data class PercentScorecardBundleAdjuster(var percent: Double) : ScorecardBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScorecardBundleAdjuster(var minimum: Double) : ScorecardBundleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScorecardBundleAdjuster(var cap: Double) : ScorecardBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedScorecardBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScorecardBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelScorecardBundleAdjuster(var factors: MutableMap<String, Double>) : ScorecardBundleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ScorecardBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scorecard bundle steps in order and keeps a trace of each. */
data class ScorecardBundleEngine(
    var steps: MutableList<ScorecardBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScorecardBundle): Double {
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

    fun evaluateAll(items: MutableList<ScorecardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScorecardBundleEngine(flags: Registry): ScorecardBundleEngine {
    return ScorecardBundleEngine(
        steps = mutableListOf<ScorecardBundleFactor>(
            PercentScorecardBundleAdjuster(2.3),
            MinimumScorecardBundleAdjuster(33.0),
            CapScorecardBundleAdjuster(18294.0),
            FlaggedScorecardBundleAdjuster(flags, "scorecard-bundle-legacy", -3.2),
            TieredScorecardBundleAdjuster(
                mutableListOf<Double>(2251.0, 7459.0),
                mutableListOf<Double>(0.88, 1.0),
            ),
            LabelScorecardBundleAdjuster(mutableMapOf<String, Double>("east" to 1.23, "retail" to 1.06)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 53975.4,
    )
}
