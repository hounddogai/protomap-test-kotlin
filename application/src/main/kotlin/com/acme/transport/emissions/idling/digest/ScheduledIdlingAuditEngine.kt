package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled idling audit calculation. */
interface ScheduledIdlingAuditAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledIdlingAudit, value: Double): Double
}

data class CapScheduledIdlingAuditAdjuster(var cap: Double) : ScheduledIdlingAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledIdlingAudit, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledIdlingAuditAdjuster(var percent: Double) : ScheduledIdlingAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledIdlingAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingScheduledIdlingAuditAdjuster(var step: Double) : ScheduledIdlingAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledIdlingAudit, value: Double): Double = roundTo(value, step)
}

data class ScheduledIdlingAuditRatioOffsetAdjuster(var weight: Double) : ScheduledIdlingAuditAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ScheduledIdlingAudit, value: Double): Double = value + item.ratio * weight
}

data class MinimumScheduledIdlingAuditAdjuster(var minimum: Double) : ScheduledIdlingAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledIdlingAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledIdlingAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledIdlingAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledIdlingAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled idling audit steps in order and keeps a trace of each. */
data class ScheduledIdlingAuditEngine(
    var steps: MutableList<ScheduledIdlingAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledIdlingAudit): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledIdlingAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledIdlingAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledIdlingAuditEngine(flags: Registry): ScheduledIdlingAuditEngine {
    return ScheduledIdlingAuditEngine(
        steps = mutableListOf<ScheduledIdlingAuditAdjuster>(
            CapScheduledIdlingAuditAdjuster(3483.0),
            PercentScheduledIdlingAuditAdjuster(-21.6),
            RoundingScheduledIdlingAuditAdjuster(5.0),
            ScheduledIdlingAuditRatioOffsetAdjuster(1.87),
            MinimumScheduledIdlingAuditAdjuster(12.2),
            FlaggedScheduledIdlingAuditAdjuster(flags, "scheduled-idling-audit-default", -6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 54177.1,
    )
}
