package com.acme.transport.safety.scorecard.batch

/** One step of the fallback scorecard batch calculation. */
interface FallbackScorecardBatchModifier {
    fun describe(): String
    fun adjust(item: FallbackScorecardBatch, value: Double): Double
}

data class LabelFallbackScorecardBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackScorecardBatchModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackScorecardBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackScorecardBatchPalletsOffsetAdjuster(var weight: Double) : FallbackScorecardBatchModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: FallbackScorecardBatch, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentFallbackScorecardBatchAdjuster(var percent: Double) : FallbackScorecardBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackScorecardBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackScorecardBatchAdjuster(var cap: Double) : FallbackScorecardBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackScorecardBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback scorecard batch steps in order and keeps a trace of each. */
data class FallbackScorecardBatchEngine(
    var steps: MutableList<FallbackScorecardBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackScorecardBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackScorecardBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackScorecardBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackScorecardBatchEngine(): FallbackScorecardBatchEngine {
    return FallbackScorecardBatchEngine(
        steps = mutableListOf<FallbackScorecardBatchModifier>(
            LabelFallbackScorecardBatchAdjuster(mutableMapOf<String, Double>("north" to 0.97)),
            FallbackScorecardBatchPalletsOffsetAdjuster(1.24),
            PercentFallbackScorecardBatchAdjuster(12.1),
            CapFallbackScorecardBatchAdjuster(3759.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 98395.4,
    )
}
