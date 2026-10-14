package com.acme.transport.safety.incident.schedule

/** One step of the batched incident audit calculation. */
interface BatchedIncidentAuditAdjuster {
    fun describe(): String
    fun adjust(item: BatchedIncidentAudit, value: Double): Double
}

data class PercentBatchedIncidentAuditAdjuster(var percent: Double) : BatchedIncidentAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedIncidentAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceBatchedIncidentAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedIncidentAuditAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedIncidentAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedIncidentAuditAdjuster(var cap: Double) : BatchedIncidentAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedIncidentAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the batched incident audit steps in order and keeps a trace of each. */
data class BatchedIncidentAuditEngine(
    var steps: MutableList<BatchedIncidentAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedIncidentAudit): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedIncidentAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedIncidentAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedIncidentAuditEngine(): BatchedIncidentAuditEngine {
    return BatchedIncidentAuditEngine(
        steps = mutableListOf<BatchedIncidentAuditAdjuster>(
            PercentBatchedIncidentAuditAdjuster(17.7),
            SourceBatchedIncidentAuditAdjuster(
                mutableMapOf<String, Double>("south" to 1.25, "east" to 0.88, "central" to 1.29),
            ),
            CapBatchedIncidentAuditAdjuster(38534.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 86416.4,
    )
}
