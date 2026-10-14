package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection batch calculation. */
interface InspectionBatchFactor {
    fun describe(): String
    fun adjust(item: InspectionBatch, value: Double): Double
}

data class TieredInspectionBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapInspectionBatchAdjuster(var cap: Double) : InspectionBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionBatch, value: Double): Double = minOf(value, cap)
}

data class InspectionBatchMarginOffsetAdjuster(var weight: Double) : InspectionBatchFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: InspectionBatch, value: Double): Double = value + item.margin * weight
}

data class FlaggedInspectionBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection batch steps in order and keeps a trace of each. */
data class InspectionBatchEngine(
    var steps: MutableList<InspectionBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionBatch): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionBatchEngine(flags: Registry): InspectionBatchEngine {
    return InspectionBatchEngine(
        steps = mutableListOf<InspectionBatchFactor>(
            TieredInspectionBatchAdjuster(
                mutableListOf<Double>(1470.0, 5989.0, 7216.0, 9688.0),
                mutableListOf<Double>(0.93, 1.07, 1.01, 1.0),
            ),
            CapInspectionBatchAdjuster(26342.0),
            InspectionBatchMarginOffsetAdjuster(1.51),
            FlaggedInspectionBatchAdjuster(flags, "inspection-batch-pending", 4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 44321.0,
    )
}
