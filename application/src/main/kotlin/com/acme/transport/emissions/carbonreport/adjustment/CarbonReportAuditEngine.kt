package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the carbon report audit calculation. */
interface CarbonReportAuditAdjuster {
    fun describe(): String
    fun adjust(item: CarbonReportAudit, value: Double): Double
}

data class CarbonReportAuditExpiresAtOffsetAdjuster(var weight: Double) : CarbonReportAuditAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CarbonReportAudit, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class FlaggedCarbonReportAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneCarbonReportAuditAdjuster(var factors: MutableMap<String, Double>) : CarbonReportAuditAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: CarbonReportAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCarbonReportAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCarbonReportAuditAdjuster(var minimum: Double) : CarbonReportAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCarbonReportAuditAdjuster(var percent: Double) : CarbonReportAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report audit steps in order and keeps a trace of each. */
data class CarbonReportAuditEngine(
    var steps: MutableList<CarbonReportAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CarbonReportAudit): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCarbonReportAuditEngine(flags: Registry): CarbonReportAuditEngine {
    return CarbonReportAuditEngine(
        steps = mutableListOf<CarbonReportAuditAdjuster>(
            CarbonReportAuditExpiresAtOffsetAdjuster(0.61),
            FlaggedCarbonReportAuditAdjuster(flags, "carbon-report-audit-rolling", -8.9),
            LaneCarbonReportAuditAdjuster(mutableMapOf<String, Double>("retail" to 1.01, "east" to 0.92)),
            TieredCarbonReportAuditAdjuster(
                mutableListOf<Double>(666.0, 9589.0),
                mutableListOf<Double>(1.06, 1.02),
            ),
            MinimumCarbonReportAuditAdjuster(27.5),
            PercentCarbonReportAuditAdjuster(-23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 55392.7,
    )
}
