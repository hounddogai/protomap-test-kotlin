package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled qualification audit calculation. */
interface ScheduledQualificationAuditStep {
    fun describe(): String
    fun adjust(item: ScheduledQualificationAudit, value: Double): Double
}

data class MinimumScheduledQualificationAuditAdjuster(var minimum: Double) : ScheduledQualificationAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledQualificationAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledQualificationAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledQualificationAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledQualificationAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleScheduledQualificationAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledQualificationAuditStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledQualificationAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledQualificationAuditAdjuster(var step: Double) : ScheduledQualificationAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledQualificationAudit, value: Double): Double = roundTo(value, step)
}

data class PercentScheduledQualificationAuditAdjuster(var percent: Double) : ScheduledQualificationAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledQualificationAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled qualification audit steps in order and keeps a trace of each. */
data class ScheduledQualificationAuditEngine(
    var steps: MutableList<ScheduledQualificationAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledQualificationAudit): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledQualificationAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledQualificationAuditEngine(flags: Registry): ScheduledQualificationAuditEngine {
    return ScheduledQualificationAuditEngine(
        steps = mutableListOf<ScheduledQualificationAuditStep>(
            MinimumScheduledQualificationAuditAdjuster(6.8),
            FlaggedScheduledQualificationAuditAdjuster(flags, "scheduled-qualification-audit-primary", 2.3),
            TitleScheduledQualificationAuditAdjuster(
                mutableMapOf<String, Double>("retail" to 1.1, "online" to 1.37, "west" to 1.26),
            ),
            RoundingScheduledQualificationAuditAdjuster(0.05),
            PercentScheduledQualificationAuditAdjuster(-13.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 52740.6,
    )
}
