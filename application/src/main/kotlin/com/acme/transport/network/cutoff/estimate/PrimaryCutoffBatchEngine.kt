package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary cutoff batch calculation. */
interface PrimaryCutoffBatchModifier {
    fun describe(): String
    fun adjust(item: PrimaryCutoffBatch, value: Double): Double
}

data class PrimaryCutoffBatchMarginOffsetAdjuster(var weight: Double) : PrimaryCutoffBatchModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: PrimaryCutoffBatch, value: Double): Double = value + item.margin * weight
}

data class RoundingPrimaryCutoffBatchAdjuster(var step: Double) : PrimaryCutoffBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCutoffBatch, value: Double): Double = roundTo(value, step)
}

data class NamePrimaryCutoffBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCutoffBatchModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryCutoffBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryCutoffBatchAdjuster(var percent: Double) : PrimaryCutoffBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCutoffBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary cutoff batch steps in order and keeps a trace of each. */
data class PrimaryCutoffBatchEngine(
    var steps: MutableList<PrimaryCutoffBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryCutoffBatch): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryCutoffBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryCutoffBatchEngine(): PrimaryCutoffBatchEngine {
    return PrimaryCutoffBatchEngine(
        steps = mutableListOf<PrimaryCutoffBatchModifier>(
            PrimaryCutoffBatchMarginOffsetAdjuster(0.9),
            RoundingPrimaryCutoffBatchAdjuster(0.5),
            NamePrimaryCutoffBatchAdjuster(
                mutableMapOf<String, Double>("south" to 1.29, "west" to 1.03, "east" to 1.2),
            ),
            PercentPrimaryCutoffBatchAdjuster(12.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 82505.6,
    )
}
