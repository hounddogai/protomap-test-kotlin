package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.flags.Registry

/** One step of the weighted backhaul audit calculation. */
interface WeightedBackhaulAuditModifier {
    fun describe(): String
    fun adjust(item: WeightedBackhaulAudit, value: Double): Double
}

data class RegionWeightedBackhaulAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedBackhaulAuditModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: WeightedBackhaulAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedBackhaulAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedBackhaulAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedBackhaulAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumWeightedBackhaulAuditAdjuster(var minimum: Double) : WeightedBackhaulAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedBackhaulAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted backhaul audit steps in order and keeps a trace of each. */
data class WeightedBackhaulAuditEngine(
    var steps: MutableList<WeightedBackhaulAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedBackhaulAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedBackhaulAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedBackhaulAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedBackhaulAuditEngine(flags: Registry): WeightedBackhaulAuditEngine {
    return WeightedBackhaulAuditEngine(
        steps = mutableListOf<WeightedBackhaulAuditModifier>(
            RegionWeightedBackhaulAuditAdjuster(
                mutableMapOf<String, Double>("online" to 0.78, "south" to 1.19, "retail" to 0.77),
            ),
            FlaggedWeightedBackhaulAuditAdjuster(flags, "weighted-backhaul-audit-pending", -4.0),
            MinimumWeightedBackhaulAuditAdjuster(20.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 67177.2,
    )
}
