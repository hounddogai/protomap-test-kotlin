package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending audit batch calculation. */
interface PendingAuditBatchStep {
    fun describe(): String
    fun adjust(item: PendingAuditBatch, value: Double): Double
}

data class RoundingPendingAuditBatchAdjuster(var step: Double) : PendingAuditBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAuditBatch, value: Double): Double = roundTo(value, step)
}

data class PendingAuditBatchRateOffsetAdjuster(var weight: Double) : PendingAuditBatchStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: PendingAuditBatch, value: Double): Double = value + item.rate * weight
}

data class FlaggedPendingAuditBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAuditBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAuditBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingAuditBatchAdjuster(var percent: Double) : PendingAuditBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingAuditBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending audit batch steps in order and keeps a trace of each. */
data class PendingAuditBatchEngine(
    var steps: MutableList<PendingAuditBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingAuditBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingAuditBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingAuditBatchEngine(flags: Registry): PendingAuditBatchEngine {
    return PendingAuditBatchEngine(
        steps = mutableListOf<PendingAuditBatchStep>(
            RoundingPendingAuditBatchAdjuster(0.5),
            PendingAuditBatchRateOffsetAdjuster(1.64),
            FlaggedPendingAuditBatchAdjuster(flags, "pending-audit-batch-batched", 0.3),
            PercentPendingAuditBatchAdjuster(-18.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 98434.8,
    )
}
