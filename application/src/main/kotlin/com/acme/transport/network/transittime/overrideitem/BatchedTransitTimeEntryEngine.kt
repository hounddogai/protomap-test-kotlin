package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched transit time entry calculation. */
interface BatchedTransitTimeEntryStep {
    fun describe(): String
    fun adjust(item: BatchedTransitTimeEntry, value: Double): Double
}

data class RoundingBatchedTransitTimeEntryAdjuster(var step: Double) : BatchedTransitTimeEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTransitTimeEntry, value: Double): Double = roundTo(value, step)
}

data class TitleBatchedTransitTimeEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTransitTimeEntryStep {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedTransitTimeEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedTransitTimeEntryAdjuster(var percent: Double) : BatchedTransitTimeEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTransitTimeEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedTransitTimeEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedTransitTimeEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedTransitTimeEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched transit time entry steps in order and keeps a trace of each. */
data class BatchedTransitTimeEntryEngine(
    var steps: MutableList<BatchedTransitTimeEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTransitTimeEntry): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTransitTimeEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTransitTimeEntryEngine(): BatchedTransitTimeEntryEngine {
    return BatchedTransitTimeEntryEngine(
        steps = mutableListOf<BatchedTransitTimeEntryStep>(
            RoundingBatchedTransitTimeEntryAdjuster(1.0),
            TitleBatchedTransitTimeEntryAdjuster(mutableMapOf<String, Double>("central" to 0.78)),
            PercentBatchedTransitTimeEntryAdjuster(17.3),
            TieredBatchedTransitTimeEntryAdjuster(
                mutableListOf<Double>(2159.0, 4094.0, 5846.0, 6589.0),
                mutableListOf<Double>(1.11, 1.14, 0.97, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 31320.7,
    )
}
