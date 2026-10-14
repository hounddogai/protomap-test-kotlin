package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending defect batch calculation. */
interface PendingDefectBatchFactor {
    fun describe(): String
    fun adjust(item: PendingDefectBatch, value: Double): Double
}

data class PendingDefectBatchVersionOffsetAdjuster(var weight: Double) : PendingDefectBatchFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PendingDefectBatch, value: Double): Double = value + item.version.toDouble() * weight
}

data class RoundingPendingDefectBatchAdjuster(var step: Double) : PendingDefectBatchFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDefectBatch, value: Double): Double = roundTo(value, step)
}

data class LabelPendingDefectBatchAdjuster(var factors: MutableMap<String, Double>) : PendingDefectBatchFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingDefectBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingDefectBatchAdjuster(var percent: Double) : PendingDefectBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDefectBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending defect batch steps in order and keeps a trace of each. */
data class PendingDefectBatchEngine(
    var steps: MutableList<PendingDefectBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDefectBatch): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDefectBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDefectBatchEngine(): PendingDefectBatchEngine {
    return PendingDefectBatchEngine(
        steps = mutableListOf<PendingDefectBatchFactor>(
            PendingDefectBatchVersionOffsetAdjuster(1.06),
            RoundingPendingDefectBatchAdjuster(5.0),
            LabelPendingDefectBatchAdjuster(mutableMapOf<String, Double>("east" to 1.08)),
            PercentPendingDefectBatchAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 64088.9,
    )
}
