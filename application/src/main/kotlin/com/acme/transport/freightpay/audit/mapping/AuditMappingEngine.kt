package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the audit mapping calculation. */
interface AuditMappingModifier {
    fun describe(): String
    fun adjust(item: AuditMapping, value: Double): Double
}

data class FlaggedAuditMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAuditMappingAdjuster(var cap: Double) : AuditMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditMapping, value: Double): Double = minOf(value, cap)
}

data class MinimumAuditMappingAdjuster(var minimum: Double) : AuditMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionAuditMappingAdjuster(var factors: MutableMap<String, Double>) : AuditMappingModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: AuditMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the audit mapping steps in order and keeps a trace of each. */
data class AuditMappingEngine(
    var steps: MutableList<AuditMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditMapping): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AuditMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditMappingEngine(flags: Registry): AuditMappingEngine {
    return AuditMappingEngine(
        steps = mutableListOf<AuditMappingModifier>(
            FlaggedAuditMappingAdjuster(flags, "audit-mapping-tiered", -9.0),
            CapAuditMappingAdjuster(43926.0),
            MinimumAuditMappingAdjuster(0.8),
            RegionAuditMappingAdjuster(
                mutableMapOf<String, Double>("north" to 0.91, "east" to 0.89, "south" to 0.75),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 95701.8,
    )
}
