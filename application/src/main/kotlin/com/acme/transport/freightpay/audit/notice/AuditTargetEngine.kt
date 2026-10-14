package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.Registry

/** One step of the audit target calculation. */
interface AuditTargetAdjuster {
    fun describe(): String
    fun adjust(item: AuditTarget, value: Double): Double
}

data class AuditTargetRetriesOffsetAdjuster(var weight: Double) : AuditTargetAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AuditTarget, value: Double): Double = value + item.retries.toDouble() * weight
}

data class CapAuditTargetAdjuster(var cap: Double) : AuditTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditTarget, value: Double): Double = minOf(value, cap)
}

data class DepotAuditTargetAdjuster(var factors: MutableMap<String, Double>) : AuditTargetAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: AuditTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAuditTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the audit target steps in order and keeps a trace of each. */
data class AuditTargetEngine(
    var steps: MutableList<AuditTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AuditTarget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAuditTargetEngine(flags: Registry): AuditTargetEngine {
    return AuditTargetEngine(
        steps = mutableListOf<AuditTargetAdjuster>(
            AuditTargetRetriesOffsetAdjuster(0.6),
            CapAuditTargetAdjuster(21049.0),
            DepotAuditTargetAdjuster(mutableMapOf<String, Double>("central" to 0.9, "online" to 1.38)),
            FlaggedAuditTargetAdjuster(flags, "audit-target-tiered", -9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 82246.1,
    )
}
