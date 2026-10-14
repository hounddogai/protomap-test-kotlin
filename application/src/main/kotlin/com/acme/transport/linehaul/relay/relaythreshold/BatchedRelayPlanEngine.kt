package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched relay plan calculation. */
interface BatchedRelayPlanModifier {
    fun describe(): String
    fun adjust(item: BatchedRelayPlan, value: Double): Double
}

data class LabelBatchedRelayPlanAdjuster(var factors: MutableMap<String, Double>) : BatchedRelayPlanModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedRelayPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedRelayPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRelayPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRelayPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedRelayPlanAdjuster(var step: Double) : BatchedRelayPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRelayPlan, value: Double): Double = roundTo(value, step)
}

data class CapBatchedRelayPlanAdjuster(var cap: Double) : BatchedRelayPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRelayPlan, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedRelayPlanAdjuster(var percent: Double) : BatchedRelayPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRelayPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched relay plan steps in order and keeps a trace of each. */
data class BatchedRelayPlanEngine(
    var steps: MutableList<BatchedRelayPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRelayPlan): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRelayPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRelayPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRelayPlanEngine(flags: Registry): BatchedRelayPlanEngine {
    return BatchedRelayPlanEngine(
        steps = mutableListOf<BatchedRelayPlanModifier>(
            LabelBatchedRelayPlanAdjuster(
                mutableMapOf<String, Double>("online" to 0.78, "west" to 1.37, "south" to 1.17),
            ),
            FlaggedBatchedRelayPlanAdjuster(flags, "batched-relay-plan-scheduled", 4.4),
            RoundingBatchedRelayPlanAdjuster(1.0),
            CapBatchedRelayPlanAdjuster(19008.0),
            PercentBatchedRelayPlanAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 53350.3,
    )
}
