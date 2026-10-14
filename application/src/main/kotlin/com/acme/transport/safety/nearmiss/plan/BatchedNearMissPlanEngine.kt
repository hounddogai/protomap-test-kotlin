package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.Registry

/** One step of the batched near miss plan calculation. */
interface BatchedNearMissPlanStep {
    fun describe(): String
    fun adjust(item: BatchedNearMissPlan, value: Double): Double
}

data class CountryBatchedNearMissPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNearMissPlanStep {
    override fun describe(): String = "by country"

    override fun adjust(item: BatchedNearMissPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedNearMissPlanAdjuster(var cap: Double) : BatchedNearMissPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedNearMissPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedNearMissPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNearMissPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNearMissPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched near miss plan steps in order and keeps a trace of each. */
data class BatchedNearMissPlanEngine(
    var steps: MutableList<BatchedNearMissPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedNearMissPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedNearMissPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedNearMissPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedNearMissPlanEngine(flags: Registry): BatchedNearMissPlanEngine {
    return BatchedNearMissPlanEngine(
        steps = mutableListOf<BatchedNearMissPlanStep>(
            CountryBatchedNearMissPlanAdjuster(mutableMapOf<String, Double>("online" to 1.11)),
            CapBatchedNearMissPlanAdjuster(13373.0),
            FlaggedBatchedNearMissPlanAdjuster(flags, "batched-near-miss-plan-manual", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 24027.6,
    )
}
