package com.acme.transport.freightpay.audit.claim

/** One step of the audit checkpoint calculation. */
interface AuditCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: AuditCheckpoint, value: Double): Double
}

data class PercentAuditCheckpointAdjuster(var percent: Double) : AuditCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAuditCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AuditCheckpointPriorityOffsetAdjuster(var weight: Double) : AuditCheckpointAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: AuditCheckpoint, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the audit checkpoint steps in order and keeps a trace of each. */
data class AuditCheckpointEngine(
    var steps: MutableList<AuditCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditCheckpoint): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditCheckpointEngine(): AuditCheckpointEngine {
    return AuditCheckpointEngine(
        steps = mutableListOf<AuditCheckpointAdjuster>(
            PercentAuditCheckpointAdjuster(-12.8),
            TieredAuditCheckpointAdjuster(
                mutableListOf<Double>(4013.0, 6816.0),
                mutableListOf<Double>(1.2, 1.04),
            ),
            AuditCheckpointPriorityOffsetAdjuster(0.15),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 17260.7,
    )
}
