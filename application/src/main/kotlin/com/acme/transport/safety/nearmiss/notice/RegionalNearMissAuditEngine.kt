package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional near miss audit calculation. */
interface RegionalNearMissAuditAdjuster {
    fun describe(): String
    fun adjust(item: RegionalNearMissAudit, value: Double): Double
}

data class RegionalNearMissAuditRateOffsetAdjuster(var weight: Double) : RegionalNearMissAuditAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RegionalNearMissAudit, value: Double): Double = value + item.rate * weight
}

data class CapRegionalNearMissAuditAdjuster(var cap: Double) : RegionalNearMissAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalNearMissAudit, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalNearMissAuditAdjuster(var minimum: Double) : RegionalNearMissAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalNearMissAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalNearMissAuditAdjuster(var percent: Double) : RegionalNearMissAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalNearMissAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalNearMissAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalNearMissAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalNearMissAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalNearMissAuditAdjuster(var step: Double) : RegionalNearMissAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalNearMissAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the regional near miss audit steps in order and keeps a trace of each. */
data class RegionalNearMissAuditEngine(
    var steps: MutableList<RegionalNearMissAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalNearMissAudit): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalNearMissAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalNearMissAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalNearMissAuditEngine(flags: Registry): RegionalNearMissAuditEngine {
    return RegionalNearMissAuditEngine(
        steps = mutableListOf<RegionalNearMissAuditAdjuster>(
            RegionalNearMissAuditRateOffsetAdjuster(1.84),
            CapRegionalNearMissAuditAdjuster(33356.0),
            MinimumRegionalNearMissAuditAdjuster(3.6),
            PercentRegionalNearMissAuditAdjuster(16.6),
            FlaggedRegionalNearMissAuditAdjuster(flags, "regional-near-miss-audit-scheduled", 6.7),
            RoundingRegionalNearMissAuditAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 27022.5,
    )
}
