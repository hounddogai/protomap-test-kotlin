package com.acme.transport.freightpay.dispute.adjustment

/** One step of the dispute audit calculation. */
interface DisputeAuditAdjuster {
    fun describe(): String
    fun adjust(item: DisputeAudit, value: Double): Double
}

data class NameDisputeAuditAdjuster(var factors: MutableMap<String, Double>) : DisputeAuditAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: DisputeAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DisputeAuditDiscountOffsetAdjuster(var weight: Double) : DisputeAuditAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DisputeAudit, value: Double): Double = value + item.discount * weight
}

data class TieredDisputeAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dispute audit steps in order and keeps a trace of each. */
data class DisputeAuditEngine(
    var steps: MutableList<DisputeAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputeAudit): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputeAuditEngine(): DisputeAuditEngine {
    return DisputeAuditEngine(
        steps = mutableListOf<DisputeAuditAdjuster>(
            NameDisputeAuditAdjuster(
                mutableMapOf<String, Double>("north" to 0.92, "south" to 1.38, "online" to 1.16),
            ),
            DisputeAuditDiscountOffsetAdjuster(0.1),
            TieredDisputeAuditAdjuster(
                mutableListOf<Double>(1538.0, 2325.0, 2652.0, 3938.0),
                mutableListOf<Double>(0.89, 0.93, 1.13, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 93410.8,
    )
}
