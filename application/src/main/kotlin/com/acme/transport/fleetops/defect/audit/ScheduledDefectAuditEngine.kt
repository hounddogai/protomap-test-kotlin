package com.acme.transport.fleetops.defect.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled defect audit calculation. */
interface ScheduledDefectAuditStep {
    fun describe(): String
    fun adjust(item: ScheduledDefectAudit, value: Double): Double
}

data class TieredScheduledDefectAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDefectAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDefectAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumScheduledDefectAuditAdjuster(var minimum: Double) : ScheduledDefectAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDefectAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledDefectAuditThresholdOffsetAdjuster(var weight: Double) : ScheduledDefectAuditStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ScheduledDefectAudit, value: Double): Double = value + item.threshold * weight
}

data class RoundingScheduledDefectAuditAdjuster(var step: Double) : ScheduledDefectAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDefectAudit, value: Double): Double = roundTo(value, step)
}

data class CapScheduledDefectAuditAdjuster(var cap: Double) : ScheduledDefectAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDefectAudit, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledDefectAuditAdjuster(var percent: Double) : ScheduledDefectAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDefectAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled defect audit steps in order and keeps a trace of each. */
data class ScheduledDefectAuditEngine(
    var steps: MutableList<ScheduledDefectAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDefectAudit): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDefectAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDefectAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDefectAuditEngine(): ScheduledDefectAuditEngine {
    return ScheduledDefectAuditEngine(
        steps = mutableListOf<ScheduledDefectAuditStep>(
            TieredScheduledDefectAuditAdjuster(
                mutableListOf<Double>(1475.0, 4823.0, 9395.0),
                mutableListOf<Double>(1.04, 0.95, 1.15),
            ),
            MinimumScheduledDefectAuditAdjuster(7.9),
            ScheduledDefectAuditThresholdOffsetAdjuster(1.23),
            RoundingScheduledDefectAuditAdjuster(0.5),
            CapScheduledDefectAuditAdjuster(22048.0),
            PercentScheduledDefectAuditAdjuster(3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 97550.6,
    )
}
