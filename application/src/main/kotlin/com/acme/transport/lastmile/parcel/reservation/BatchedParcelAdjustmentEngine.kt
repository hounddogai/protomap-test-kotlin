package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched parcel adjustment calculation. */
interface BatchedParcelAdjustmentStep {
    fun describe(): String
    fun adjust(item: BatchedParcelAdjustment, value: Double): Double
}

data class FlaggedBatchedParcelAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedParcelAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedParcelAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelBatchedParcelAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedParcelAdjustmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedParcelAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedParcelAdjustmentAdjuster(var step: Double) : BatchedParcelAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedParcelAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedParcelAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedParcelAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedParcelAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedParcelAdjustmentAdjuster(var percent: Double) : BatchedParcelAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedParcelAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched parcel adjustment steps in order and keeps a trace of each. */
data class BatchedParcelAdjustmentEngine(
    var steps: MutableList<BatchedParcelAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedParcelAdjustment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedParcelAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedParcelAdjustmentEngine(flags: Registry): BatchedParcelAdjustmentEngine {
    return BatchedParcelAdjustmentEngine(
        steps = mutableListOf<BatchedParcelAdjustmentStep>(
            FlaggedBatchedParcelAdjustmentAdjuster(flags, "batched-parcel-adjustment-pending", -2.1),
            LabelBatchedParcelAdjustmentAdjuster(mutableMapOf<String, Double>("central" to 0.94)),
            RoundingBatchedParcelAdjustmentAdjuster(0.05),
            TieredBatchedParcelAdjustmentAdjuster(
                mutableListOf<Double>(4326.0, 5907.0),
                mutableListOf<Double>(1.16, 1.01),
            ),
            PercentBatchedParcelAdjustmentAdjuster(6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 14733.6,
    )
}
