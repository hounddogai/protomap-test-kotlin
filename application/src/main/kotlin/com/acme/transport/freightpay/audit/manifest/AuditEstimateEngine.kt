package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the audit estimate calculation. */
interface AuditEstimateModifier {
    fun describe(): String
    fun adjust(item: AuditEstimate, value: Double): Double
}

data class PercentAuditEstimateAdjuster(var percent: Double) : AuditEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAuditEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAuditEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAuditEstimateAdjuster(var minimum: Double) : AuditEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AuditEstimateVersionOffsetAdjuster(var weight: Double) : AuditEstimateModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: AuditEstimate, value: Double): Double = value + item.version.toDouble() * weight
}

data class LaneAuditEstimateAdjuster(var factors: MutableMap<String, Double>) : AuditEstimateModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: AuditEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the audit estimate steps in order and keeps a trace of each. */
data class AuditEstimateEngine(
    var steps: MutableList<AuditEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditEstimate): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AuditEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditEstimateEngine(flags: Registry): AuditEstimateEngine {
    return AuditEstimateEngine(
        steps = mutableListOf<AuditEstimateModifier>(
            PercentAuditEstimateAdjuster(2.9),
            TieredAuditEstimateAdjuster(
                mutableListOf<Double>(2251.0, 5838.0),
                mutableListOf<Double>(1.17, 1.18),
            ),
            FlaggedAuditEstimateAdjuster(flags, "audit-estimate-default", 8.5),
            MinimumAuditEstimateAdjuster(7.1),
            AuditEstimateVersionOffsetAdjuster(1.34),
            LaneAuditEstimateAdjuster(mutableMapOf<String, Double>("east" to 1.01, "south" to 0.9)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 75220.2,
    )
}
