package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered route offset milestone calculation. */
interface TieredRouteOffsetMilestoneStep {
    fun describe(): String
    fun adjust(item: TieredRouteOffsetMilestone, value: Double): Double
}

data class RoundingTieredRouteOffsetMilestoneAdjuster(var step: Double) : TieredRouteOffsetMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRouteOffsetMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredRouteOffsetMilestoneAdjuster(var minimum: Double) : TieredRouteOffsetMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRouteOffsetMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitTieredRouteOffsetMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRouteOffsetMilestoneStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: TieredRouteOffsetMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredRouteOffsetMilestoneAdjuster(var cap: Double) : TieredRouteOffsetMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredRouteOffsetMilestone, value: Double): Double = minOf(value, cap)
}

data class TieredRouteOffsetMilestoneCreatedAtOffsetAdjuster(
    var weight: Double,
) : TieredRouteOffsetMilestoneStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TieredRouteOffsetMilestone, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the tiered route offset milestone steps in order and keeps a trace of each. */
data class TieredRouteOffsetMilestoneEngine(
    var steps: MutableList<TieredRouteOffsetMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredRouteOffsetMilestone): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRouteOffsetMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredRouteOffsetMilestoneEngine(): TieredRouteOffsetMilestoneEngine {
    return TieredRouteOffsetMilestoneEngine(
        steps = mutableListOf<TieredRouteOffsetMilestoneStep>(
            RoundingTieredRouteOffsetMilestoneAdjuster(5.0),
            MinimumTieredRouteOffsetMilestoneAdjuster(28.0),
            UnitTieredRouteOffsetMilestoneAdjuster(
                mutableMapOf<String, Double>("north" to 0.88, "east" to 1.13),
            ),
            CapTieredRouteOffsetMilestoneAdjuster(13016.0),
            TieredRouteOffsetMilestoneCreatedAtOffsetAdjuster(0.21),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 77827.5,
    )
}
