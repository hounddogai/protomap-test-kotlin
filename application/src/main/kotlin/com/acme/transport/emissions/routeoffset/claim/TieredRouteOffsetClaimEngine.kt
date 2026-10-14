package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered route offset claim calculation. */
interface TieredRouteOffsetClaimStep {
    fun describe(): String
    fun adjust(item: TieredRouteOffsetClaim, value: Double): Double
}

data class TieredTieredRouteOffsetClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRouteOffsetClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRouteOffsetClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredRouteOffsetClaimAdjuster(var cap: Double) : TieredRouteOffsetClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredRouteOffsetClaim, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredRouteOffsetClaimAdjuster(var step: Double) : TieredRouteOffsetClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRouteOffsetClaim, value: Double): Double = roundTo(value, step)
}

data class SegmentTieredRouteOffsetClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRouteOffsetClaimStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredRouteOffsetClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredRouteOffsetClaimAdjuster(var minimum: Double) : TieredRouteOffsetClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRouteOffsetClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered route offset claim steps in order and keeps a trace of each. */
data class TieredRouteOffsetClaimEngine(
    var steps: MutableList<TieredRouteOffsetClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRouteOffsetClaim): Double {
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

    fun evaluateAll(items: MutableList<TieredRouteOffsetClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRouteOffsetClaimEngine(): TieredRouteOffsetClaimEngine {
    return TieredRouteOffsetClaimEngine(
        steps = mutableListOf<TieredRouteOffsetClaimStep>(
            TieredTieredRouteOffsetClaimAdjuster(
                mutableListOf<Double>(1169.0, 2348.0),
                mutableListOf<Double>(0.88, 1.04),
            ),
            CapTieredRouteOffsetClaimAdjuster(42532.0),
            RoundingTieredRouteOffsetClaimAdjuster(0.1),
            SegmentTieredRouteOffsetClaimAdjuster(mutableMapOf<String, Double>("online" to 0.95)),
            MinimumTieredRouteOffsetClaimAdjuster(3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 80628.5,
    )
}
