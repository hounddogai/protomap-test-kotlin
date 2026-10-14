package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit profile calculation. */
interface AuditProfileAdjuster {
    fun describe(): String
    fun adjust(item: AuditProfile, value: Double): Double
}

data class RoundingAuditProfileAdjuster(var step: Double) : AuditProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditProfile, value: Double): Double = roundTo(value, step)
}

data class FlaggedAuditProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAuditProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAuditProfileAdjuster(var percent: Double) : AuditProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the audit profile steps in order and keeps a trace of each. */
data class AuditProfileEngine(
    var steps: MutableList<AuditProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditProfile): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditProfileEngine(flags: Registry): AuditProfileEngine {
    return AuditProfileEngine(
        steps = mutableListOf<AuditProfileAdjuster>(
            RoundingAuditProfileAdjuster(5.0),
            FlaggedAuditProfileAdjuster(flags, "audit-profile-tiered", -5.8),
            TieredAuditProfileAdjuster(
                mutableListOf<Double>(3394.0, 4590.0, 9126.0),
                mutableListOf<Double>(1.07, 0.98, 1.18),
            ),
            PercentAuditProfileAdjuster(0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 93040.8,
    )
}
