package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional utilization segment calculation. */
interface RegionalUtilizationSegmentModifier {
    fun describe(): String
    fun adjust(item: RegionalUtilizationSegment, value: Double): Double
}

data class PercentRegionalUtilizationSegmentAdjuster(var percent: Double) : RegionalUtilizationSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalUtilizationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalUtilizationSegmentExpiresAtOffsetAdjuster(
    var weight: Double,
) : RegionalUtilizationSegmentModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RegionalUtilizationSegment, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class FlaggedRegionalUtilizationSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalUtilizationSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalUtilizationSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalUtilizationSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalUtilizationSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalUtilizationSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional utilization segment steps in order and keeps a trace of each. */
data class RegionalUtilizationSegmentEngine(
    var steps: MutableList<RegionalUtilizationSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalUtilizationSegment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalUtilizationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalUtilizationSegmentEngine(flags: Registry): RegionalUtilizationSegmentEngine {
    return RegionalUtilizationSegmentEngine(
        steps = mutableListOf<RegionalUtilizationSegmentModifier>(
            PercentRegionalUtilizationSegmentAdjuster(23.6),
            RegionalUtilizationSegmentExpiresAtOffsetAdjuster(1.44),
            FlaggedRegionalUtilizationSegmentAdjuster(flags, "regional-utilization-segment-default", -5.4),
            TieredRegionalUtilizationSegmentAdjuster(
                mutableListOf<Double>(242.0, 3053.0, 3807.0),
                mutableListOf<Double>(0.88, 1.04, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 44765.6,
    )
}
