package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.Registry

/** One step of the strict drop audit calculation. */
interface StrictDropAuditAdjuster {
    fun describe(): String
    fun adjust(item: StrictDropAudit, value: Double): Double
}

data class PercentStrictDropAuditAdjuster(var percent: Double) : StrictDropAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDropAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictDropAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDropAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDropAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictDropAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDropAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDropAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict drop audit steps in order and keeps a trace of each. */
data class StrictDropAuditEngine(
    var steps: MutableList<StrictDropAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictDropAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDropAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDropAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictDropAuditEngine(flags: Registry): StrictDropAuditEngine {
    return StrictDropAuditEngine(
        steps = mutableListOf<StrictDropAuditAdjuster>(
            PercentStrictDropAuditAdjuster(-24.3),
            FlaggedStrictDropAuditAdjuster(flags, "strict-drop-audit-tiered", 3.2),
            TieredStrictDropAuditAdjuster(
                mutableListOf<Double>(1936.0, 5921.0, 8346.0),
                mutableListOf<Double>(1.07, 1.12, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 71935.3,
    )
}
