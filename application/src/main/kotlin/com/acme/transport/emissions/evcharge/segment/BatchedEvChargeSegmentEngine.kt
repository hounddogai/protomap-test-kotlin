package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched ev charge segment calculation. */
interface BatchedEvChargeSegmentStep {
    fun describe(): String
    fun adjust(item: BatchedEvChargeSegment, value: Double): Double
}

data class RoundingBatchedEvChargeSegmentAdjuster(var step: Double) : BatchedEvChargeSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedEvChargeSegment, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedEvChargeSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedEvChargeSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedEvChargeSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedEvChargeSegmentEtaMinutesOffsetAdjuster(var weight: Double) : BatchedEvChargeSegmentStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: BatchedEvChargeSegment, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class MinimumBatchedEvChargeSegmentAdjuster(var minimum: Double) : BatchedEvChargeSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedEvChargeSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedEvChargeSegmentAdjuster(var percent: Double) : BatchedEvChargeSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedEvChargeSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedEvChargeSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedEvChargeSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedEvChargeSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched ev charge segment steps in order and keeps a trace of each. */
data class BatchedEvChargeSegmentEngine(
    var steps: MutableList<BatchedEvChargeSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedEvChargeSegment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedEvChargeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedEvChargeSegmentEngine(flags: Registry): BatchedEvChargeSegmentEngine {
    return BatchedEvChargeSegmentEngine(
        steps = mutableListOf<BatchedEvChargeSegmentStep>(
            RoundingBatchedEvChargeSegmentAdjuster(0.1),
            TieredBatchedEvChargeSegmentAdjuster(
                mutableListOf<Double>(1013.0, 1436.0, 9005.0),
                mutableListOf<Double>(1.04, 1.15, 0.87),
            ),
            BatchedEvChargeSegmentEtaMinutesOffsetAdjuster(1.46),
            MinimumBatchedEvChargeSegmentAdjuster(43.3),
            PercentBatchedEvChargeSegmentAdjuster(-16.5),
            FlaggedBatchedEvChargeSegmentAdjuster(flags, "batched-ev-charge-segment-rolling", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 70859.4,
    )
}
