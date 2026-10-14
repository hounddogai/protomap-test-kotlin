package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery audit calculation. */
interface RedeliveryAuditFactor {
    fun describe(): String
    fun adjust(item: RedeliveryAudit, value: Double): Double
}

data class RoundingRedeliveryAuditAdjuster(var step: Double) : RedeliveryAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryAudit, value: Double): Double = roundTo(value, step)
}

data class CapRedeliveryAuditAdjuster(var cap: Double) : RedeliveryAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryAudit, value: Double): Double = minOf(value, cap)
}

data class PercentRedeliveryAuditAdjuster(var percent: Double) : RedeliveryAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRedeliveryAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the redelivery audit steps in order and keeps a trace of each. */
data class RedeliveryAuditEngine(
    var steps: MutableList<RedeliveryAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryAuditEngine(flags: Registry): RedeliveryAuditEngine {
    return RedeliveryAuditEngine(
        steps = mutableListOf<RedeliveryAuditFactor>(
            RoundingRedeliveryAuditAdjuster(1.0),
            CapRedeliveryAuditAdjuster(23269.0),
            PercentRedeliveryAuditAdjuster(0.3),
            FlaggedRedeliveryAuditAdjuster(flags, "redelivery-audit-pending", 5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 29682.0,
    )
}
