package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending audit score calculation. */
interface PendingAuditScoreModifier {
    fun describe(): String
    fun adjust(item: PendingAuditScore, value: Double): Double
}

data class PendingAuditScoreGraceDaysOffsetAdjuster(var weight: Double) : PendingAuditScoreModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: PendingAuditScore, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class FlaggedPendingAuditScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAuditScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAuditScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingAuditScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAuditScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAuditScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingAuditScoreAdjuster(var step: Double) : PendingAuditScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAuditScore, value: Double): Double = roundTo(value, step)
}

data class CapPendingAuditScoreAdjuster(var cap: Double) : PendingAuditScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingAuditScore, value: Double): Double = minOf(value, cap)
}

data class TitlePendingAuditScoreAdjuster(var factors: MutableMap<String, Double>) : PendingAuditScoreModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingAuditScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending audit score steps in order and keeps a trace of each. */
data class PendingAuditScoreEngine(
    var steps: MutableList<PendingAuditScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingAuditScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAuditScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAuditScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingAuditScoreEngine(flags: Registry): PendingAuditScoreEngine {
    return PendingAuditScoreEngine(
        steps = mutableListOf<PendingAuditScoreModifier>(
            PendingAuditScoreGraceDaysOffsetAdjuster(0.42),
            FlaggedPendingAuditScoreAdjuster(flags, "pending-audit-score-adaptive", 0.4),
            TieredPendingAuditScoreAdjuster(
                mutableListOf<Double>(405.0, 4761.0),
                mutableListOf<Double>(0.82, 1.05),
            ),
            RoundingPendingAuditScoreAdjuster(0.1),
            CapPendingAuditScoreAdjuster(8718.0),
            TitlePendingAuditScoreAdjuster(mutableMapOf<String, Double>("west" to 1.0)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 48869.7,
    )
}
