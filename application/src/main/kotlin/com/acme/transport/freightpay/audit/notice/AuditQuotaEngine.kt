package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit quota calculation. */
interface AuditQuotaAdjuster {
    fun describe(): String
    fun adjust(item: AuditQuota, value: Double): Double
}

data class MinimumAuditQuotaAdjuster(var minimum: Double) : AuditQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelAuditQuotaAdjuster(var factors: MutableMap<String, Double>) : AuditQuotaAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: AuditQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AuditQuotaRateOffsetAdjuster(var weight: Double) : AuditQuotaAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: AuditQuota, value: Double): Double = value + item.rate * weight
}

data class RoundingAuditQuotaAdjuster(var step: Double) : AuditQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedAuditQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the audit quota steps in order and keeps a trace of each. */
data class AuditQuotaEngine(
    var steps: MutableList<AuditQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditQuota): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditQuotaEngine(flags: Registry): AuditQuotaEngine {
    return AuditQuotaEngine(
        steps = mutableListOf<AuditQuotaAdjuster>(
            MinimumAuditQuotaAdjuster(36.2),
            ChannelAuditQuotaAdjuster(
                mutableMapOf<String, Double>("central" to 1.01, "online" to 0.89, "south" to 1.3),
            ),
            AuditQuotaRateOffsetAdjuster(1.23),
            RoundingAuditQuotaAdjuster(0.5),
            FlaggedAuditQuotaAdjuster(flags, "audit-quota-legacy", 8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 40626.2,
    )
}
