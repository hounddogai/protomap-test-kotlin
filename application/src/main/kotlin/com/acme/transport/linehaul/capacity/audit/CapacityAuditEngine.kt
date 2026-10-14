package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity audit calculation. */
interface CapacityAuditModifier {
    fun describe(): String
    fun adjust(item: CapacityAudit, value: Double): Double
}

data class TieredCapacityAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCapacityAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapacityAuditEtaMinutesOffsetAdjuster(var weight: Double) : CapacityAuditModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: CapacityAudit, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class CapCapacityAuditAdjuster(var cap: Double) : CapacityAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityAudit, value: Double): Double = minOf(value, cap)
}

data class TitleCapacityAuditAdjuster(var factors: MutableMap<String, Double>) : CapacityAuditModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CapacityAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCapacityAuditAdjuster(var step: Double) : CapacityAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity audit steps in order and keeps a trace of each. */
data class CapacityAuditEngine(
    var steps: MutableList<CapacityAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityAudit): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityAuditEngine(flags: Registry): CapacityAuditEngine {
    return CapacityAuditEngine(
        steps = mutableListOf<CapacityAuditModifier>(
            TieredCapacityAuditAdjuster(
                mutableListOf<Double>(2508.0, 2657.0, 5943.0, 6230.0),
                mutableListOf<Double>(1.01, 1.05, 0.99, 0.8),
            ),
            FlaggedCapacityAuditAdjuster(flags, "capacity-audit-primary", 3.3),
            CapacityAuditEtaMinutesOffsetAdjuster(1.28),
            CapCapacityAuditAdjuster(48011.0),
            TitleCapacityAuditAdjuster(mutableMapOf<String, Double>("central" to 0.93)),
            RoundingCapacityAuditAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 86104.2,
    )
}
