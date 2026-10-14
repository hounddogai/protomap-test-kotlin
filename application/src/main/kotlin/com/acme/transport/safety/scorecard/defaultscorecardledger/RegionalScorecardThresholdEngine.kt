package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional scorecard threshold calculation. */
interface RegionalScorecardThresholdFactor {
    fun describe(): String
    fun adjust(item: RegionalScorecardThreshold, value: Double): Double
}

data class CapRegionalScorecardThresholdAdjuster(var cap: Double) : RegionalScorecardThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalScorecardThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalScorecardThresholdAdjuster(var percent: Double) : RegionalScorecardThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalScorecardThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalScorecardThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalScorecardThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalScorecardThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalScorecardThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalScorecardThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalScorecardThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRegionalScorecardThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalScorecardThresholdFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalScorecardThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional scorecard threshold steps in order and keeps a trace of each. */
data class RegionalScorecardThresholdEngine(
    var steps: MutableList<RegionalScorecardThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalScorecardThreshold): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalScorecardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalScorecardThresholdEngine(flags: Registry): RegionalScorecardThresholdEngine {
    return RegionalScorecardThresholdEngine(
        steps = mutableListOf<RegionalScorecardThresholdFactor>(
            CapRegionalScorecardThresholdAdjuster(46288.0),
            PercentRegionalScorecardThresholdAdjuster(-22.8),
            TieredRegionalScorecardThresholdAdjuster(
                mutableListOf<Double>(1768.0, 7032.0, 7568.0),
                mutableListOf<Double>(0.95, 1.19, 1.03),
            ),
            FlaggedRegionalScorecardThresholdAdjuster(flags, "regional-scorecard-threshold-primary", -5.7),
            NameRegionalScorecardThresholdAdjuster(
                mutableMapOf<String, Double>("west" to 0.73, "retail" to 1.25),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 48628.8,
    )
}
