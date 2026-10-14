package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched qualification bundle calculation. */
interface BatchedQualificationBundleAdjuster {
    fun describe(): String
    fun adjust(item: BatchedQualificationBundle, value: Double): Double
}

data class BatchedQualificationBundleMarginOffsetAdjuster(
    var weight: Double,
) : BatchedQualificationBundleAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: BatchedQualificationBundle, value: Double): Double = value + item.margin * weight
}

data class FlaggedBatchedQualificationBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedQualificationBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedQualificationBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedQualificationBundleAdjuster(var percent: Double) : BatchedQualificationBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedQualificationBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedQualificationBundleAdjuster(var step: Double) : BatchedQualificationBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedQualificationBundle, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedQualificationBundleAdjuster(var minimum: Double) : BatchedQualificationBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedQualificationBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched qualification bundle steps in order and keeps a trace of each. */
data class BatchedQualificationBundleEngine(
    var steps: MutableList<BatchedQualificationBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedQualificationBundle): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedQualificationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedQualificationBundleEngine(flags: Registry): BatchedQualificationBundleEngine {
    return BatchedQualificationBundleEngine(
        steps = mutableListOf<BatchedQualificationBundleAdjuster>(
            BatchedQualificationBundleMarginOffsetAdjuster(1.22),
            FlaggedBatchedQualificationBundleAdjuster(flags, "batched-qualification-bundle-legacy", -4.5),
            PercentBatchedQualificationBundleAdjuster(-14.2),
            RoundingBatchedQualificationBundleAdjuster(1.0),
            MinimumBatchedQualificationBundleAdjuster(36.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 65808.7,
    )
}
