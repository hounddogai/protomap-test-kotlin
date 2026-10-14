package com.acme.transport.crewing.restbreak.breaksummary

/** One step of the batched rest break summary calculation. */
interface BatchedRestBreakSummaryStep {
    fun describe(): String
    fun adjust(item: BatchedRestBreakSummary, value: Double): Double
}

data class PercentBatchedRestBreakSummaryAdjuster(var percent: Double) : BatchedRestBreakSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRestBreakSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameBatchedRestBreakSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRestBreakSummaryStep {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedRestBreakSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedRestBreakSummaryPriorityOffsetAdjuster(var weight: Double) : BatchedRestBreakSummaryStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: BatchedRestBreakSummary, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the batched rest break summary steps in order and keeps a trace of each. */
data class BatchedRestBreakSummaryEngine(
    var steps: MutableList<BatchedRestBreakSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedRestBreakSummary): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRestBreakSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRestBreakSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedRestBreakSummaryEngine(): BatchedRestBreakSummaryEngine {
    return BatchedRestBreakSummaryEngine(
        steps = mutableListOf<BatchedRestBreakSummaryStep>(
            PercentBatchedRestBreakSummaryAdjuster(-23.0),
            NameBatchedRestBreakSummaryAdjuster(
                mutableMapOf<String, Double>("central" to 0.88, "retail" to 1.18, "east" to 1.13),
            ),
            BatchedRestBreakSummaryPriorityOffsetAdjuster(0.07),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 84074.5,
    )
}
