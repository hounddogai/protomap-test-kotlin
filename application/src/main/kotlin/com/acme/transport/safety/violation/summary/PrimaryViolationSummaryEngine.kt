package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary violation summary calculation. */
interface PrimaryViolationSummaryFactor {
    fun describe(): String
    fun adjust(item: PrimaryViolationSummary, value: Double): Double
}

data class SourcePrimaryViolationSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryViolationSummaryFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: PrimaryViolationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPrimaryViolationSummaryAdjuster(var cap: Double) : PrimaryViolationSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryViolationSummary, value: Double): Double = minOf(value, cap)
}

data class PrimaryViolationSummaryCostOffsetAdjuster(var weight: Double) : PrimaryViolationSummaryFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: PrimaryViolationSummary, value: Double): Double = value + item.cost * weight
}

/** Runs the primary violation summary steps in order and keeps a trace of each. */
data class PrimaryViolationSummaryEngine(
    var steps: MutableList<PrimaryViolationSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryViolationSummary): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryViolationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryViolationSummaryEngine(): PrimaryViolationSummaryEngine {
    return PrimaryViolationSummaryEngine(
        steps = mutableListOf<PrimaryViolationSummaryFactor>(
            SourcePrimaryViolationSummaryAdjuster(
                mutableMapOf<String, Double>("retail" to 0.7, "north" to 1.34, "south" to 1.03),
            ),
            CapPrimaryViolationSummaryAdjuster(2825.0),
            PrimaryViolationSummaryCostOffsetAdjuster(0.55),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 88995.1,
    )
}
