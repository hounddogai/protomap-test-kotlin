package com.acme.transport.lastmile.drop.batcheddropnotice

/** One step of the drop batch calculation. */
interface DropBatchStep {
    fun describe(): String
    fun adjust(item: DropBatch, value: Double): Double
}

data class TitleDropBatchAdjuster(var factors: MutableMap<String, Double>) : DropBatchStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DropBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDropBatchAdjuster(var cap: Double) : DropBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropBatch, value: Double): Double = minOf(value, cap)
}

data class DropBatchRatioOffsetAdjuster(var weight: Double) : DropBatchStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: DropBatch, value: Double): Double = value + item.ratio * weight
}

data class PercentDropBatchAdjuster(var percent: Double) : DropBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the drop batch steps in order and keeps a trace of each. */
data class DropBatchEngine(
    var steps: MutableList<DropBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DropBatch): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<DropBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDropBatchEngine(): DropBatchEngine {
    return DropBatchEngine(
        steps = mutableListOf<DropBatchStep>(
            TitleDropBatchAdjuster(mutableMapOf<String, Double>("central" to 1.01, "retail" to 1.39)),
            CapDropBatchAdjuster(39811.0),
            DropBatchRatioOffsetAdjuster(1.58),
            PercentDropBatchAdjuster(5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 37180.7,
    )
}
