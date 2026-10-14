package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped inspection batch calculation. */
interface CappedInspectionBatchAdjuster {
    fun describe(): String
    fun adjust(item: CappedInspectionBatch, value: Double): Double
}

data class CapCappedInspectionBatchAdjuster(var cap: Double) : CappedInspectionBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedInspectionBatch, value: Double): Double = minOf(value, cap)
}

data class PercentCappedInspectionBatchAdjuster(var percent: Double) : CappedInspectionBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedInspectionBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedInspectionBatchScoreOffsetAdjuster(var weight: Double) : CappedInspectionBatchAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CappedInspectionBatch, value: Double): Double = value + item.score * weight
}

data class MinimumCappedInspectionBatchAdjuster(var minimum: Double) : CappedInspectionBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedInspectionBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelCappedInspectionBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedInspectionBatchAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedInspectionBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedInspectionBatchAdjuster(var step: Double) : CappedInspectionBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedInspectionBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the capped inspection batch steps in order and keeps a trace of each. */
data class CappedInspectionBatchEngine(
    var steps: MutableList<CappedInspectionBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedInspectionBatch): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedInspectionBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedInspectionBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedInspectionBatchEngine(): CappedInspectionBatchEngine {
    return CappedInspectionBatchEngine(
        steps = mutableListOf<CappedInspectionBatchAdjuster>(
            CapCappedInspectionBatchAdjuster(39434.0),
            PercentCappedInspectionBatchAdjuster(-10.1),
            CappedInspectionBatchScoreOffsetAdjuster(1.11),
            MinimumCappedInspectionBatchAdjuster(13.5),
            LabelCappedInspectionBatchAdjuster(mutableMapOf<String, Double>("south" to 0.94, "online" to 1.29)),
            RoundingCappedInspectionBatchAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 39876.2,
    )
}
