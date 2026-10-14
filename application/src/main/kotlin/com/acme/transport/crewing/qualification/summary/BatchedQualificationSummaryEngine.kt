package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched qualification summary calculation. */
interface BatchedQualificationSummaryStep {
    fun describe(): String
    fun adjust(item: BatchedQualificationSummary, value: Double): Double
}

data class TieredBatchedQualificationSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedQualificationSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedQualificationSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotBatchedQualificationSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedQualificationSummaryStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedQualificationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedQualificationSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedQualificationSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedQualificationSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedQualificationSummaryDurationMinutesOffsetAdjuster(
    var weight: Double,
) : BatchedQualificationSummaryStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: BatchedQualificationSummary, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the batched qualification summary steps in order and keeps a trace of each. */
data class BatchedQualificationSummaryEngine(
    var steps: MutableList<BatchedQualificationSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedQualificationSummary): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedQualificationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedQualificationSummaryEngine(flags: Registry): BatchedQualificationSummaryEngine {
    return BatchedQualificationSummaryEngine(
        steps = mutableListOf<BatchedQualificationSummaryStep>(
            TieredBatchedQualificationSummaryAdjuster(
                mutableListOf<Double>(5683.0, 6724.0),
                mutableListOf<Double>(1.15, 0.84),
            ),
            DepotBatchedQualificationSummaryAdjuster(
                mutableMapOf<String, Double>("central" to 1.16, "north" to 1.27),
            ),
            FlaggedBatchedQualificationSummaryAdjuster(flags, "batched-qualification-summary-fallback", -9.9),
            BatchedQualificationSummaryDurationMinutesOffsetAdjuster(0.93),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 90520.5,
    )
}
