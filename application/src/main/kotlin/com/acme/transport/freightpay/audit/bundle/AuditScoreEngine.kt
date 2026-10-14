package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit score calculation. */
interface AuditScoreAdjuster {
    fun describe(): String
    fun adjust(item: AuditScore, value: Double): Double
}

data class FlaggedAuditScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAuditScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAuditScoreAdjuster(var cap: Double) : AuditScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditScore, value: Double): Double = minOf(value, cap)
}

data class PercentAuditScoreAdjuster(var percent: Double) : AuditScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAuditScoreAdjuster(var step: Double) : AuditScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditScore, value: Double): Double = roundTo(value, step)
}

data class AuditScorePalletsOffsetAdjuster(var weight: Double) : AuditScoreAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: AuditScore, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the audit score steps in order and keeps a trace of each. */
data class AuditScoreEngine(
    var steps: MutableList<AuditScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditScore): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditScoreEngine(flags: Registry): AuditScoreEngine {
    return AuditScoreEngine(
        steps = mutableListOf<AuditScoreAdjuster>(
            FlaggedAuditScoreAdjuster(flags, "audit-score-adaptive", 1.1),
            TieredAuditScoreAdjuster(
                mutableListOf<Double>(2957.0, 8868.0, 9999.0),
                mutableListOf<Double>(1.09, 1.17, 1.13),
            ),
            CapAuditScoreAdjuster(1300.0),
            PercentAuditScoreAdjuster(13.4),
            RoundingAuditScoreAdjuster(0.5),
            AuditScorePalletsOffsetAdjuster(0.13),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 20918.2,
    )
}
