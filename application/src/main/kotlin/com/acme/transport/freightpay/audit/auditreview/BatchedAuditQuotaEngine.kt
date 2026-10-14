package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched audit quota calculation. */
interface BatchedAuditQuotaFactor {
    fun describe(): String
    fun adjust(item: BatchedAuditQuota, value: Double): Double
}

data class PercentBatchedAuditQuotaAdjuster(var percent: Double) : BatchedAuditQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAuditQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedAuditQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAuditQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAuditQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedAuditQuotaAdjuster(var step: Double) : BatchedAuditQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedAuditQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the batched audit quota steps in order and keeps a trace of each. */
data class BatchedAuditQuotaEngine(
    var steps: MutableList<BatchedAuditQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedAuditQuota): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedAuditQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedAuditQuotaEngine(flags: Registry): BatchedAuditQuotaEngine {
    return BatchedAuditQuotaEngine(
        steps = mutableListOf<BatchedAuditQuotaFactor>(
            PercentBatchedAuditQuotaAdjuster(24.8),
            FlaggedBatchedAuditQuotaAdjuster(flags, "batched-audit-quota-manual", -2.4),
            RoundingBatchedAuditQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 55659.0,
    )
}
