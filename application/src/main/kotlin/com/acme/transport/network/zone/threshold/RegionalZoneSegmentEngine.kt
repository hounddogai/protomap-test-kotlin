package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional zone segment calculation. */
interface RegionalZoneSegmentStep {
    fun describe(): String
    fun adjust(item: RegionalZoneSegment, value: Double): Double
}

data class FlaggedRegionalZoneSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalZoneSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalZoneSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalZoneSegmentAdjuster(var cap: Double) : RegionalZoneSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalZoneSegment, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalZoneSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalZoneSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalZoneSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitRegionalZoneSegmentAdjuster(var factors: MutableMap<String, Double>) : RegionalZoneSegmentStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalZoneSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional zone segment steps in order and keeps a trace of each. */
data class RegionalZoneSegmentEngine(
    var steps: MutableList<RegionalZoneSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalZoneSegment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalZoneSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalZoneSegmentEngine(flags: Registry): RegionalZoneSegmentEngine {
    return RegionalZoneSegmentEngine(
        steps = mutableListOf<RegionalZoneSegmentStep>(
            FlaggedRegionalZoneSegmentAdjuster(flags, "regional-zone-segment-tiered", 0.4),
            CapRegionalZoneSegmentAdjuster(20195.0),
            TieredRegionalZoneSegmentAdjuster(
                mutableListOf<Double>(4379.0, 4580.0),
                mutableListOf<Double>(1.19, 0.88),
            ),
            UnitRegionalZoneSegmentAdjuster(mutableMapOf<String, Double>("central" to 1.0)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 45736.5,
    )
}
