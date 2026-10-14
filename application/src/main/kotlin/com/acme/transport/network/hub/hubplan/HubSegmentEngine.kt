package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub segment calculation. */
interface HubSegmentModifier {
    fun describe(): String
    fun adjust(item: HubSegment, value: Double): Double
}

data class RoundingHubSegmentAdjuster(var step: Double) : HubSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubSegment, value: Double): Double = roundTo(value, step)
}

data class TieredHubSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedHubSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleHubSegmentAdjuster(var factors: MutableMap<String, Double>) : HubSegmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: HubSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapHubSegmentAdjuster(var cap: Double) : HubSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumHubSegmentAdjuster(var minimum: Double) : HubSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hub segment steps in order and keeps a trace of each. */
data class HubSegmentEngine(
    var steps: MutableList<HubSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HubSegment): Double {
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

    fun evaluateAll(items: MutableList<HubSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHubSegmentEngine(flags: Registry): HubSegmentEngine {
    return HubSegmentEngine(
        steps = mutableListOf<HubSegmentModifier>(
            RoundingHubSegmentAdjuster(0.05),
            TieredHubSegmentAdjuster(
                mutableListOf<Double>(113.0, 462.0, 4185.0),
                mutableListOf<Double>(0.88, 1.03, 1.19),
            ),
            FlaggedHubSegmentAdjuster(flags, "hub-segment-primary", 0.6),
            TitleHubSegmentAdjuster(
                mutableMapOf<String, Double>("west" to 1.4, "north" to 0.95, "east" to 1.11),
            ),
            CapHubSegmentAdjuster(11920.0),
            MinimumHubSegmentAdjuster(31.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 77475.2,
    )
}
