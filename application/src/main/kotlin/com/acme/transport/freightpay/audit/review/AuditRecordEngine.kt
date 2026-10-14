package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.numbers.roundTo

/** One step of the audit record calculation. */
interface AuditRecordFactor {
    fun describe(): String
    fun adjust(item: AuditRecord, value: Double): Double
}

data class CapAuditRecordAdjuster(var cap: Double) : AuditRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditRecord, value: Double): Double = minOf(value, cap)
}

data class TieredAuditRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionAuditRecordAdjuster(var factors: MutableMap<String, Double>) : AuditRecordFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: AuditRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAuditRecordAdjuster(var percent: Double) : AuditRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAuditRecordAdjuster(var minimum: Double) : AuditRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAuditRecordAdjuster(var step: Double) : AuditRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the audit record steps in order and keeps a trace of each. */
data class AuditRecordEngine(
    var steps: MutableList<AuditRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditRecord): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AuditRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditRecordEngine(): AuditRecordEngine {
    return AuditRecordEngine(
        steps = mutableListOf<AuditRecordFactor>(
            CapAuditRecordAdjuster(30494.0),
            TieredAuditRecordAdjuster(mutableListOf<Double>(39.0, 6479.0), mutableListOf<Double>(0.91, 1.07)),
            RegionAuditRecordAdjuster(mutableMapOf<String, Double>("retail" to 1.02, "east" to 0.83)),
            PercentAuditRecordAdjuster(2.7),
            MinimumAuditRecordAdjuster(43.3),
            RoundingAuditRecordAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 4465.1,
    )
}
