package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender batch calculation. */
interface TenderBatchFactor {
    fun describe(): String
    fun adjust(item: TenderBatch, value: Double): Double
}

data class CapTenderBatchAdjuster(var cap: Double) : TenderBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderBatch, value: Double): Double = minOf(value, cap)
}

data class NameTenderBatchAdjuster(var factors: MutableMap<String, Double>) : TenderBatchFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TenderBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TenderBatchRevisionOffsetAdjuster(var weight: Double) : TenderBatchFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: TenderBatch, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the tender batch steps in order and keeps a trace of each. */
data class TenderBatchEngine(
    var steps: MutableList<TenderBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderBatch): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderBatchEngine(): TenderBatchEngine {
    return TenderBatchEngine(
        steps = mutableListOf<TenderBatchFactor>(
            CapTenderBatchAdjuster(2744.0),
            NameTenderBatchAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
            TenderBatchRevisionOffsetAdjuster(0.76),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 80500.6,
    )
}
