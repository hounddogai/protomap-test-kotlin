package com.acme.transport.network.hub.audit

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled hub audit calculation. */
interface ScheduledHubAuditFactor {
    fun describe(): String
    fun adjust(item: ScheduledHubAudit, value: Double): Double
}

data class MinimumScheduledHubAuditAdjuster(var minimum: Double) : ScheduledHubAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledHubAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledHubAuditThresholdOffsetAdjuster(var weight: Double) : ScheduledHubAuditFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ScheduledHubAudit, value: Double): Double = value + item.threshold * weight
}

data class TieredScheduledHubAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledHubAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledHubAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledHubAuditAdjuster(var cap: Double) : ScheduledHubAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledHubAudit, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledHubAuditAdjuster(var percent: Double) : ScheduledHubAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledHubAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledHubAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHubAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHubAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled hub audit steps in order and keeps a trace of each. */
data class ScheduledHubAuditEngine(
    var steps: MutableList<ScheduledHubAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledHubAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledHubAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledHubAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledHubAuditEngine(flags: Registry): ScheduledHubAuditEngine {
    return ScheduledHubAuditEngine(
        steps = mutableListOf<ScheduledHubAuditFactor>(
            MinimumScheduledHubAuditAdjuster(33.1),
            ScheduledHubAuditThresholdOffsetAdjuster(0.59),
            TieredScheduledHubAuditAdjuster(
                mutableListOf<Double>(2728.0, 4110.0),
                mutableListOf<Double>(1.04, 1.13),
            ),
            CapScheduledHubAuditAdjuster(35896.0),
            PercentScheduledHubAuditAdjuster(-13.7),
            FlaggedScheduledHubAuditAdjuster(flags, "scheduled-hub-audit-weighted", -1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 90050.3,
    )
}
