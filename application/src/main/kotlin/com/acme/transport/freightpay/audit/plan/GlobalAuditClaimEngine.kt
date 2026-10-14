package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.flags.Registry

/** One step of the global audit claim calculation. */
interface GlobalAuditClaimModifier {
    fun describe(): String
    fun adjust(item: GlobalAuditClaim, value: Double): Double
}

data class ChannelGlobalAuditClaimAdjuster(var factors: MutableMap<String, Double>) : GlobalAuditClaimModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: GlobalAuditClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalAuditClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAuditClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAuditClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalAuditClaimAdjuster(var minimum: Double) : GlobalAuditClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalAuditClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global audit claim steps in order and keeps a trace of each. */
data class GlobalAuditClaimEngine(
    var steps: MutableList<GlobalAuditClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalAuditClaim): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalAuditClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalAuditClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalAuditClaimEngine(flags: Registry): GlobalAuditClaimEngine {
    return GlobalAuditClaimEngine(
        steps = mutableListOf<GlobalAuditClaimModifier>(
            ChannelGlobalAuditClaimAdjuster(mutableMapOf<String, Double>("south" to 0.81, "online" to 0.71)),
            FlaggedGlobalAuditClaimAdjuster(flags, "global-audit-claim-capped", 1.2),
            MinimumGlobalAuditClaimAdjuster(37.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 97117.9,
    )
}
