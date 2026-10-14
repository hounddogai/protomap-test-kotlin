package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.flags.Registry

/** One step of the idling batch calculation. */
interface IdlingBatchStep {
    fun describe(): String
    fun adjust(item: IdlingBatch, value: Double): Double
}

data class TitleIdlingBatchAdjuster(var factors: MutableMap<String, Double>) : IdlingBatchStep {
    override fun describe(): String = "by title"

    override fun adjust(item: IdlingBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class IdlingBatchAttemptsOffsetAdjuster(var weight: Double) : IdlingBatchStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: IdlingBatch, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class FlaggedIdlingBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentIdlingBatchAdjuster(var percent: Double) : IdlingBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the idling batch steps in order and keeps a trace of each. */
data class IdlingBatchEngine(
    var steps: MutableList<IdlingBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingBatch): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingBatchEngine(flags: Registry): IdlingBatchEngine {
    return IdlingBatchEngine(
        steps = mutableListOf<IdlingBatchStep>(
            TitleIdlingBatchAdjuster(mutableMapOf<String, Double>("central" to 1.15, "west" to 0.87)),
            IdlingBatchAttemptsOffsetAdjuster(1.41),
            FlaggedIdlingBatchAdjuster(flags, "idling-batch-legacy", -8.7),
            PercentIdlingBatchAdjuster(20.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 79608.0,
    )
}
