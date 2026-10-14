package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster threshold calculation. */
interface RosterThresholdAdjuster {
    fun describe(): String
    fun adjust(item: RosterThreshold, value: Double): Double
}

data class FlaggedRosterThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRosterThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRosterThresholdAdjuster(var percent: Double) : RosterThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster threshold steps in order and keeps a trace of each. */
data class RosterThresholdEngine(
    var steps: MutableList<RosterThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterThreshold): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterThresholdEngine(flags: Registry): RosterThresholdEngine {
    return RosterThresholdEngine(
        steps = mutableListOf<RosterThresholdAdjuster>(
            FlaggedRosterThresholdAdjuster(flags, "roster-threshold-rolling", 7.8),
            TieredRosterThresholdAdjuster(
                mutableListOf<Double>(771.0, 8933.0),
                mutableListOf<Double>(1.01, 0.9),
            ),
            PercentRosterThresholdAdjuster(-5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 82935.2,
    )
}
