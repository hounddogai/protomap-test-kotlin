package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary audit batch calculation. */
interface PrimaryAuditBatchFactor {
    fun describe(): String
    fun adjust(item: PrimaryAuditBatch, value: Double): Double
}

data class PrimaryAuditBatchStopCountOffsetAdjuster(var weight: Double) : PrimaryAuditBatchFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PrimaryAuditBatch, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredPrimaryAuditBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryAuditBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryAuditBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPrimaryAuditBatchAdjuster(var cap: Double) : PrimaryAuditBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAuditBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryAuditBatchAdjuster(var minimum: Double) : PrimaryAuditBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAuditBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryAuditBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryAuditBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryAuditBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary audit batch steps in order and keeps a trace of each. */
data class PrimaryAuditBatchEngine(
    var steps: MutableList<PrimaryAuditBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryAuditBatch): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryAuditBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryAuditBatchEngine(flags: Registry): PrimaryAuditBatchEngine {
    return PrimaryAuditBatchEngine(
        steps = mutableListOf<PrimaryAuditBatchFactor>(
            PrimaryAuditBatchStopCountOffsetAdjuster(1.71),
            TieredPrimaryAuditBatchAdjuster(
                mutableListOf<Double>(20.0, 785.0, 5951.0, 8290.0),
                mutableListOf<Double>(0.92, 1.06, 0.95, 1.02),
            ),
            CapPrimaryAuditBatchAdjuster(20467.0),
            MinimumPrimaryAuditBatchAdjuster(4.2),
            FlaggedPrimaryAuditBatchAdjuster(flags, "primary-audit-batch-primary", 7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 90943.2,
    )
}
