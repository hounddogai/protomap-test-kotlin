package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal dispute segment calculation. */
interface SeasonalDisputeSegmentFactor {
    fun describe(): String
    fun adjust(item: SeasonalDisputeSegment, value: Double): Double
}

data class TieredSeasonalDisputeSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDisputeSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDisputeSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalDisputeSegmentDistanceKmOffsetAdjuster(var weight: Double) : SeasonalDisputeSegmentFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: SeasonalDisputeSegment, value: Double): Double = value + item.distanceKm * weight
}

data class CapSeasonalDisputeSegmentAdjuster(var cap: Double) : SeasonalDisputeSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDisputeSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalDisputeSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDisputeSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDisputeSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalDisputeSegmentAdjuster(var minimum: Double) : SeasonalDisputeSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDisputeSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleSeasonalDisputeSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDisputeSegmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalDisputeSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal dispute segment steps in order and keeps a trace of each. */
data class SeasonalDisputeSegmentEngine(
    var steps: MutableList<SeasonalDisputeSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalDisputeSegment): Double {
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

    fun evaluateAll(items: MutableList<SeasonalDisputeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalDisputeSegmentEngine(flags: Registry): SeasonalDisputeSegmentEngine {
    return SeasonalDisputeSegmentEngine(
        steps = mutableListOf<SeasonalDisputeSegmentFactor>(
            TieredSeasonalDisputeSegmentAdjuster(
                mutableListOf<Double>(5448.0, 8373.0, 9324.0),
                mutableListOf<Double>(0.95, 0.97, 1.18),
            ),
            SeasonalDisputeSegmentDistanceKmOffsetAdjuster(0.5),
            CapSeasonalDisputeSegmentAdjuster(32858.0),
            FlaggedSeasonalDisputeSegmentAdjuster(flags, "seasonal-dispute-segment-primary", -3.0),
            MinimumSeasonalDisputeSegmentAdjuster(23.6),
            TitleSeasonalDisputeSegmentAdjuster(
                mutableMapOf<String, Double>("west" to 1.16, "north" to 0.82, "retail" to 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 27919.4,
    )
}
