package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive route offset snapshot calculation. */
interface AdaptiveRouteOffsetSnapshotStep {
    fun describe(): String
    fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double
}

data class AdaptiveRouteOffsetSnapshotRetriesOffsetAdjuster(
    var weight: Double,
) : AdaptiveRouteOffsetSnapshotStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class NameAdaptiveRouteOffsetSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveRouteOffsetSnapshotStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveRouteOffsetSnapshotAdjuster(var percent: Double) : AdaptiveRouteOffsetSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveRouteOffsetSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRouteOffsetSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveRouteOffsetSnapshotAdjuster(var cap: Double) : AdaptiveRouteOffsetSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveRouteOffsetSnapshotAdjuster(var minimum: Double) : AdaptiveRouteOffsetSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRouteOffsetSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive route offset snapshot steps in order and keeps a trace of each. */
data class AdaptiveRouteOffsetSnapshotEngine(
    var steps: MutableList<AdaptiveRouteOffsetSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveRouteOffsetSnapshot): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveRouteOffsetSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveRouteOffsetSnapshotEngine(): AdaptiveRouteOffsetSnapshotEngine {
    return AdaptiveRouteOffsetSnapshotEngine(
        steps = mutableListOf<AdaptiveRouteOffsetSnapshotStep>(
            AdaptiveRouteOffsetSnapshotRetriesOffsetAdjuster(0.44),
            NameAdaptiveRouteOffsetSnapshotAdjuster(
                mutableMapOf<String, Double>("south" to 1.28, "central" to 0.97),
            ),
            PercentAdaptiveRouteOffsetSnapshotAdjuster(6.9),
            TieredAdaptiveRouteOffsetSnapshotAdjuster(
                mutableListOf<Double>(1286.0, 7916.0, 8975.0, 8993.0),
                mutableListOf<Double>(0.9, 0.81, 0.92, 0.85),
            ),
            CapAdaptiveRouteOffsetSnapshotAdjuster(6840.0),
            MinimumAdaptiveRouteOffsetSnapshotAdjuster(41.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 78002.5,
    )
}
