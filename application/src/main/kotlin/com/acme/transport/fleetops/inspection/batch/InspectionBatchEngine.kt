package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection batch calculation. */
interface InspectionBatchAdjuster {
    fun describe(): String
    fun adjust(item: InspectionBatch, value: Double): Double
}

data class CapInspectionBatchAdjuster(var cap: Double) : InspectionBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionBatch, value: Double): Double = minOf(value, cap)
}

data class InspectionBatchAmountOffsetAdjuster(var weight: Double) : InspectionBatchAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: InspectionBatch, value: Double): Double = value + item.amount * weight
}

data class TieredInspectionBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionBatchAdjuster {
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

data class RoundingInspectionBatchAdjuster(var step: Double) : InspectionBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the inspection batch steps in order and keeps a trace of each. */
data class InspectionBatchEngine(
    var steps: MutableList<InspectionBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionBatch): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionBatchEngine(): InspectionBatchEngine {
    return InspectionBatchEngine(
        steps = mutableListOf<InspectionBatchAdjuster>(
            CapInspectionBatchAdjuster(26822.0),
            InspectionBatchAmountOffsetAdjuster(0.73),
            TieredInspectionBatchAdjuster(
                mutableListOf<Double>(5336.0, 7365.0, 9348.0),
                mutableListOf<Double>(1.17, 0.83, 1.14),
            ),
            RoundingInspectionBatchAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 97712.9,
    )
}
