package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive availability audit calculation. */
interface AdaptiveAvailabilityAuditAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAvailabilityAudit, value: Double): Double
}

data class TieredAdaptiveAvailabilityAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAvailabilityAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAvailabilityAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveAvailabilityAuditAdjuster(var minimum: Double) : AdaptiveAvailabilityAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAvailabilityAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveAvailabilityAuditAdjuster(var cap: Double) : AdaptiveAvailabilityAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAvailabilityAudit, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveAvailabilityAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAvailabilityAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAvailabilityAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive availability audit steps in order and keeps a trace of each. */
data class AdaptiveAvailabilityAuditEngine(
    var steps: MutableList<AdaptiveAvailabilityAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAvailabilityAudit): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAvailabilityAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAvailabilityAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAvailabilityAuditEngine(flags: Registry): AdaptiveAvailabilityAuditEngine {
    return AdaptiveAvailabilityAuditEngine(
        steps = mutableListOf<AdaptiveAvailabilityAuditAdjuster>(
            TieredAdaptiveAvailabilityAuditAdjuster(
                mutableListOf<Double>(4382.0, 8139.0),
                mutableListOf<Double>(0.85, 1.05),
            ),
            MinimumAdaptiveAvailabilityAuditAdjuster(13.3),
            CapAdaptiveAvailabilityAuditAdjuster(18629.0),
            FlaggedAdaptiveAvailabilityAuditAdjuster(flags, "adaptive-availability-audit-strict", 5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 68868.1,
    )
}
