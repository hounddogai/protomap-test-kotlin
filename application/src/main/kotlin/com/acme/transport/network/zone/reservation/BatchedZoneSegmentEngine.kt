package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the batched zone segment calculation. */
interface BatchedZoneSegmentStep {
    fun describe(): String
    fun adjust(item: BatchedZoneSegment, value: Double): Double
}

data class TieredBatchedZoneSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedZoneSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedZoneSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedZoneSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedZoneSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedZoneSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedZoneSegmentAdjuster(var minimum: Double) : BatchedZoneSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedZoneSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotBatchedZoneSegmentAdjuster(var factors: MutableMap<String, Double>) : BatchedZoneSegmentStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedZoneSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedZoneSegmentAdjuster(var cap: Double) : BatchedZoneSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedZoneSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the batched zone segment steps in order and keeps a trace of each. */
data class BatchedZoneSegmentEngine(
    var steps: MutableList<BatchedZoneSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedZoneSegment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedZoneSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedZoneSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedZoneSegmentEngine(flags: Registry): BatchedZoneSegmentEngine {
    return BatchedZoneSegmentEngine(
        steps = mutableListOf<BatchedZoneSegmentStep>(
            TieredBatchedZoneSegmentAdjuster(
                mutableListOf<Double>(2256.0, 3424.0, 4397.0, 7225.0),
                mutableListOf<Double>(1.01, 1.08, 1.15, 0.94),
            ),
            FlaggedBatchedZoneSegmentAdjuster(flags, "batched-zone-segment-regional", -7.0),
            MinimumBatchedZoneSegmentAdjuster(49.7),
            DepotBatchedZoneSegmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.25, "south" to 1.3, "online" to 0.91),
            ),
            CapBatchedZoneSegmentAdjuster(40935.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 71984.2,
    )
}
