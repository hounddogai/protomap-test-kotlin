package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.flags.Registry

/** One step of the batched route offset digest calculation. */
interface BatchedRouteOffsetDigestAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRouteOffsetDigest, value: Double): Double
}

data class MinimumBatchedRouteOffsetDigestAdjuster(var minimum: Double) : BatchedRouteOffsetDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRouteOffsetDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedRouteOffsetDigestDistanceKmOffsetAdjuster(
    var weight: Double,
) : BatchedRouteOffsetDigestAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: BatchedRouteOffsetDigest, value: Double): Double = value + item.distanceKm * weight
}

data class PercentBatchedRouteOffsetDigestAdjuster(var percent: Double) : BatchedRouteOffsetDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRouteOffsetDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryBatchedRouteOffsetDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRouteOffsetDigestAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: BatchedRouteOffsetDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedRouteOffsetDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRouteOffsetDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRouteOffsetDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched route offset digest steps in order and keeps a trace of each. */
data class BatchedRouteOffsetDigestEngine(
    var steps: MutableList<BatchedRouteOffsetDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedRouteOffsetDigest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRouteOffsetDigestAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRouteOffsetDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedRouteOffsetDigestEngine(flags: Registry): BatchedRouteOffsetDigestEngine {
    return BatchedRouteOffsetDigestEngine(
        steps = mutableListOf<BatchedRouteOffsetDigestAdjuster>(
            MinimumBatchedRouteOffsetDigestAdjuster(31.5),
            BatchedRouteOffsetDigestDistanceKmOffsetAdjuster(1.61),
            PercentBatchedRouteOffsetDigestAdjuster(-10.5),
            CategoryBatchedRouteOffsetDigestAdjuster(mutableMapOf<String, Double>("online" to 1.15)),
            FlaggedBatchedRouteOffsetDigestAdjuster(flags, "batched-route-offset-digest-legacy", -8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 91702.1,
    )
}
