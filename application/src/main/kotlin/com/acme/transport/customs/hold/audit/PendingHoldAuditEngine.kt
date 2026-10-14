package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.flags.Registry

/** One step of the pending hold audit calculation. */
interface PendingHoldAuditFactor {
    fun describe(): String
    fun adjust(item: PendingHoldAudit, value: Double): Double
}

data class PendingHoldAuditVersionOffsetAdjuster(var weight: Double) : PendingHoldAuditFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PendingHoldAudit, value: Double): Double = value + item.version.toDouble() * weight
}

data class MinimumPendingHoldAuditAdjuster(var minimum: Double) : PendingHoldAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHoldAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitlePendingHoldAuditAdjuster(var factors: MutableMap<String, Double>) : PendingHoldAuditFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingHoldAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingHoldAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingHoldAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingHoldAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingHoldAuditAdjuster(var percent: Double) : PendingHoldAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingHoldAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending hold audit steps in order and keeps a trace of each. */
data class PendingHoldAuditEngine(
    var steps: MutableList<PendingHoldAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingHoldAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingHoldAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingHoldAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingHoldAuditEngine(flags: Registry): PendingHoldAuditEngine {
    return PendingHoldAuditEngine(
        steps = mutableListOf<PendingHoldAuditFactor>(
            PendingHoldAuditVersionOffsetAdjuster(0.68),
            MinimumPendingHoldAuditAdjuster(29.8),
            TitlePendingHoldAuditAdjuster(
                mutableMapOf<String, Double>("online" to 1.12, "south" to 0.97, "west" to 0.81),
            ),
            FlaggedPendingHoldAuditAdjuster(flags, "pending-hold-audit-pending", 5.2),
            PercentPendingHoldAuditAdjuster(-1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 18164.6,
    )
}
