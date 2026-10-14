package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit window calculation. */
interface AuditWindowAdjuster {
    fun describe(): String
    fun adjust(item: AuditWindow, value: Double): Double
}

data class TitleAuditWindowAdjuster(var factors: MutableMap<String, Double>) : AuditWindowAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AuditWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAuditWindowAdjuster(var cap: Double) : AuditWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditWindow, value: Double): Double = minOf(value, cap)
}

data class TieredAuditWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAuditWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAuditWindowAdjuster(var step: Double) : AuditWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditWindow, value: Double): Double = roundTo(value, step)
}

data class AuditWindowScoreOffsetAdjuster(var weight: Double) : AuditWindowAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: AuditWindow, value: Double): Double = value + item.score * weight
}

/** Runs the audit window steps in order and keeps a trace of each. */
data class AuditWindowEngine(
    var steps: MutableList<AuditWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditWindow): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditWindowEngine(flags: Registry): AuditWindowEngine {
    return AuditWindowEngine(
        steps = mutableListOf<AuditWindowAdjuster>(
            TitleAuditWindowAdjuster(mutableMapOf<String, Double>("west" to 0.85)),
            CapAuditWindowAdjuster(49344.0),
            TieredAuditWindowAdjuster(mutableListOf<Double>(3991.0, 4185.0), mutableListOf<Double>(1.08, 0.9)),
            FlaggedAuditWindowAdjuster(flags, "audit-window-default", -5.7),
            RoundingAuditWindowAdjuster(1.0),
            AuditWindowScoreOffsetAdjuster(0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 59094.0,
    )
}
