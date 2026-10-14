package com.acme.transport.freightpay.audit.policy

/** One step of the audit override calculation. */
interface AuditOverrideStep {
    fun describe(): String
    fun adjust(item: AuditOverride, value: Double): Double
}

data class TieredAuditOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryAuditOverrideAdjuster(var factors: MutableMap<String, Double>) : AuditOverrideStep {
    override fun describe(): String = "by category"

    override fun adjust(item: AuditOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAuditOverrideAdjuster(var minimum: Double) : AuditOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the audit override steps in order and keeps a trace of each. */
data class AuditOverrideEngine(
    var steps: MutableList<AuditOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditOverride): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<AuditOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditOverrideEngine(): AuditOverrideEngine {
    return AuditOverrideEngine(
        steps = mutableListOf<AuditOverrideStep>(
            TieredAuditOverrideAdjuster(
                mutableListOf<Double>(3445.0, 6780.0, 7422.0, 8552.0),
                mutableListOf<Double>(1.2, 1.19, 0.85, 0.96),
            ),
            CategoryAuditOverrideAdjuster(mutableMapOf<String, Double>("retail" to 0.9)),
            MinimumAuditOverrideAdjuster(44.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 58877.7,
    )
}
