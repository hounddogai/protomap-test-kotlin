package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped audit digest calculation. */
interface CappedAuditDigestStep {
    fun describe(): String
    fun adjust(item: CappedAuditDigest, value: Double): Double
}

data class RoundingCappedAuditDigestAdjuster(var step: Double) : CappedAuditDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAuditDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedAuditDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAuditDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAuditDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedAuditDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAuditDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAuditDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped audit digest steps in order and keeps a trace of each. */
data class CappedAuditDigestEngine(
    var steps: MutableList<CappedAuditDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedAuditDigest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAuditDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAuditDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedAuditDigestEngine(flags: Registry): CappedAuditDigestEngine {
    return CappedAuditDigestEngine(
        steps = mutableListOf<CappedAuditDigestStep>(
            RoundingCappedAuditDigestAdjuster(0.5),
            FlaggedCappedAuditDigestAdjuster(flags, "capped-audit-digest-rolling", 5.1),
            TieredCappedAuditDigestAdjuster(
                mutableListOf<Double>(756.0, 1931.0, 7793.0),
                mutableListOf<Double>(1.14, 0.87, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 15092.9,
    )
}
