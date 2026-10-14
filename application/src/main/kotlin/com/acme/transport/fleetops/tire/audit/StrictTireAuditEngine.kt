package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict tire audit calculation. */
interface StrictTireAuditStep {
    fun describe(): String
    fun adjust(item: StrictTireAudit, value: Double): Double
}

data class FlaggedStrictTireAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTireAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTireAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictTireAuditAdjuster(var minimum: Double) : StrictTireAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTireAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictTireAuditAdjuster(var cap: Double) : StrictTireAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTireAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictTireAuditAdjuster(var step: Double) : StrictTireAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTireAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the strict tire audit steps in order and keeps a trace of each. */
data class StrictTireAuditEngine(
    var steps: MutableList<StrictTireAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTireAudit): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTireAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTireAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTireAuditEngine(flags: Registry): StrictTireAuditEngine {
    return StrictTireAuditEngine(
        steps = mutableListOf<StrictTireAuditStep>(
            FlaggedStrictTireAuditAdjuster(flags, "strict-tire-audit-weighted", 6.1),
            MinimumStrictTireAuditAdjuster(33.0),
            CapStrictTireAuditAdjuster(10630.0),
            RoundingStrictTireAuditAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 33589.5,
    )
}
