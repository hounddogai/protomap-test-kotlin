package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual ev charge audit calculation. */
interface ManualEvChargeAuditFactor {
    fun describe(): String
    fun adjust(item: ManualEvChargeAudit, value: Double): Double
}

data class MinimumManualEvChargeAuditAdjuster(var minimum: Double) : ManualEvChargeAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualEvChargeAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualEvChargeAuditAdjuster(var percent: Double) : ManualEvChargeAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualEvChargeAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleManualEvChargeAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualEvChargeAuditFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualEvChargeAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualEvChargeAuditAdjuster(var step: Double) : ManualEvChargeAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualEvChargeAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualEvChargeAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualEvChargeAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualEvChargeAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual ev charge audit steps in order and keeps a trace of each. */
data class ManualEvChargeAuditEngine(
    var steps: MutableList<ManualEvChargeAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualEvChargeAudit): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualEvChargeAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualEvChargeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualEvChargeAuditEngine(flags: Registry): ManualEvChargeAuditEngine {
    return ManualEvChargeAuditEngine(
        steps = mutableListOf<ManualEvChargeAuditFactor>(
            MinimumManualEvChargeAuditAdjuster(40.4),
            PercentManualEvChargeAuditAdjuster(23.7),
            LocaleManualEvChargeAuditAdjuster(mutableMapOf<String, Double>("retail" to 0.73, "east" to 0.95)),
            RoundingManualEvChargeAuditAdjuster(1.0),
            FlaggedManualEvChargeAuditAdjuster(flags, "manual-ev-charge-audit-scheduled", 3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 52092.7,
    )
}
