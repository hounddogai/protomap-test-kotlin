package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched inspection template calculation. */
interface BatchedInspectionTemplateFactor {
    fun describe(): String
    fun adjust(item: BatchedInspectionTemplate, value: Double): Double
}

data class TieredBatchedInspectionTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedInspectionTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedInspectionTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedInspectionTemplateAdjuster(var percent: Double) : BatchedInspectionTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedInspectionTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedInspectionTemplateAdjuster(var minimum: Double) : BatchedInspectionTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedInspectionTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedInspectionTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedInspectionTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedInspectionTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameBatchedInspectionTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedInspectionTemplateFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedInspectionTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched inspection template steps in order and keeps a trace of each. */
data class BatchedInspectionTemplateEngine(
    var steps: MutableList<BatchedInspectionTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedInspectionTemplate): Double {
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

    fun evaluateAll(items: MutableList<BatchedInspectionTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedInspectionTemplateEngine(flags: Registry): BatchedInspectionTemplateEngine {
    return BatchedInspectionTemplateEngine(
        steps = mutableListOf<BatchedInspectionTemplateFactor>(
            TieredBatchedInspectionTemplateAdjuster(
                mutableListOf<Double>(3882.0, 5120.0),
                mutableListOf<Double>(0.88, 1.09),
            ),
            PercentBatchedInspectionTemplateAdjuster(1.5),
            MinimumBatchedInspectionTemplateAdjuster(41.1),
            FlaggedBatchedInspectionTemplateAdjuster(flags, "batched-inspection-template-regional", -4.3),
            NameBatchedInspectionTemplateAdjuster(
                mutableMapOf<String, Double>("west" to 1.34, "central" to 0.72),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 60132.2,
    )
}
