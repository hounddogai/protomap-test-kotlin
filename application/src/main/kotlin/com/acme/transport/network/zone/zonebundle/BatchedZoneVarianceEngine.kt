package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched zone variance calculation. */
interface BatchedZoneVarianceModifier {
    fun describe(): String
    fun adjust(item: BatchedZoneVariance, value: Double): Double
}

data class RoundingBatchedZoneVarianceAdjuster(var step: Double) : BatchedZoneVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedZoneVariance, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedZoneVarianceAdjuster(var percent: Double) : BatchedZoneVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedZoneVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedZoneVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedZoneVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedZoneVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentBatchedZoneVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedZoneVarianceModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: BatchedZoneVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedZoneVarianceAdjuster(var minimum: Double) : BatchedZoneVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedZoneVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched zone variance steps in order and keeps a trace of each. */
data class BatchedZoneVarianceEngine(
    var steps: MutableList<BatchedZoneVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedZoneVariance): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedZoneVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedZoneVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedZoneVarianceEngine(flags: Registry): BatchedZoneVarianceEngine {
    return BatchedZoneVarianceEngine(
        steps = mutableListOf<BatchedZoneVarianceModifier>(
            RoundingBatchedZoneVarianceAdjuster(0.25),
            PercentBatchedZoneVarianceAdjuster(-18.3),
            FlaggedBatchedZoneVarianceAdjuster(flags, "batched-zone-variance-lenient", 5.7),
            SegmentBatchedZoneVarianceAdjuster(mutableMapOf<String, Double>("west" to 1.36, "central" to 1.24)),
            MinimumBatchedZoneVarianceAdjuster(32.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 29480.1,
    )
}
