package com.acme.transport.freightpay.audit.auditpolicy

/** One step of the weighted audit segment calculation. */
interface WeightedAuditSegmentStep {
    fun describe(): String
    fun adjust(item: WeightedAuditSegment, value: Double): Double
}

data class TitleWeightedAuditSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAuditSegmentStep {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedAuditSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedAuditSegmentAdjuster(var cap: Double) : WeightedAuditSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAuditSegment, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedAuditSegmentAdjuster(var percent: Double) : WeightedAuditSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAuditSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted audit segment steps in order and keeps a trace of each. */
data class WeightedAuditSegmentEngine(
    var steps: MutableList<WeightedAuditSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedAuditSegment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAuditSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAuditSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedAuditSegmentEngine(): WeightedAuditSegmentEngine {
    return WeightedAuditSegmentEngine(
        steps = mutableListOf<WeightedAuditSegmentStep>(
            TitleWeightedAuditSegmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.35, "south" to 0.95, "online" to 0.79),
            ),
            CapWeightedAuditSegmentAdjuster(3453.0),
            PercentWeightedAuditSegmentAdjuster(-2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 58450.8,
    )
}
