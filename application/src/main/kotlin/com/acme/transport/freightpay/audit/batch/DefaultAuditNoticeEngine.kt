package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.flags.Registry

/** One step of the default audit notice calculation. */
interface DefaultAuditNoticeStep {
    fun describe(): String
    fun adjust(item: DefaultAuditNotice, value: Double): Double
}

data class DefaultAuditNoticeOdometerOffsetAdjuster(var weight: Double) : DefaultAuditNoticeStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: DefaultAuditNotice, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class FlaggedDefaultAuditNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAuditNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAuditNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefaultAuditNoticeAdjuster(var minimum: Double) : DefaultAuditNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAuditNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultAuditNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAuditNoticeStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAuditNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default audit notice steps in order and keeps a trace of each. */
data class DefaultAuditNoticeEngine(
    var steps: MutableList<DefaultAuditNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultAuditNotice): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAuditNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAuditNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultAuditNoticeEngine(flags: Registry): DefaultAuditNoticeEngine {
    return DefaultAuditNoticeEngine(
        steps = mutableListOf<DefaultAuditNoticeStep>(
            DefaultAuditNoticeOdometerOffsetAdjuster(1.41),
            FlaggedDefaultAuditNoticeAdjuster(flags, "default-audit-notice-seasonal", 5.3),
            MinimumDefaultAuditNoticeAdjuster(0.8),
            TieredDefaultAuditNoticeAdjuster(
                mutableListOf<Double>(627.0, 1769.0, 6089.0, 6484.0),
                mutableListOf<Double>(1.19, 1.09, 0.82, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 61153.5,
    )
}
