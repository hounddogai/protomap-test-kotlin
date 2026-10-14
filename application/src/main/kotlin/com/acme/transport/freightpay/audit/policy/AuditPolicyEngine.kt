package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the audit policy calculation. */
interface AuditPolicyFactor {
    fun describe(): String
    fun adjust(item: AuditPolicy, value: Double): Double
}

data class TitleAuditPolicyAdjuster(var factors: MutableMap<String, Double>) : AuditPolicyFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: AuditPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAuditPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAuditPolicyAdjuster(var percent: Double) : AuditPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AuditPolicyMinimumOffsetAdjuster(var weight: Double) : AuditPolicyFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: AuditPolicy, value: Double): Double = value + item.minimum * weight
}

data class RoundingAuditPolicyAdjuster(var step: Double) : AuditPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the audit policy steps in order and keeps a trace of each. */
data class AuditPolicyEngine(
    var steps: MutableList<AuditPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditPolicy): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AuditPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditPolicyEngine(): AuditPolicyEngine {
    return AuditPolicyEngine(
        steps = mutableListOf<AuditPolicyFactor>(
            TitleAuditPolicyAdjuster(mutableMapOf<String, Double>("north" to 0.89, "central" to 1.2)),
            TieredAuditPolicyAdjuster(mutableListOf<Double>(5919.0, 9334.0), mutableListOf<Double>(1.19, 1.0)),
            PercentAuditPolicyAdjuster(18.9),
            AuditPolicyMinimumOffsetAdjuster(1.95),
            RoundingAuditPolicyAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 79937.5,
    )
}
