package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit notice calculation. */
interface AuditNoticeAdjuster {
    fun describe(): String
    fun adjust(item: AuditNotice, value: Double): Double
}

data class CapAuditNoticeAdjuster(var cap: Double) : AuditNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedAuditNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAuditNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the audit notice steps in order and keeps a trace of each. */
data class AuditNoticeEngine(
    var steps: MutableList<AuditNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AuditNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAuditNoticeEngine(flags: Registry): AuditNoticeEngine {
    return AuditNoticeEngine(
        steps = mutableListOf<AuditNoticeAdjuster>(
            CapAuditNoticeAdjuster(26013.0),
            FlaggedAuditNoticeAdjuster(flags, "audit-notice-global", -1.5),
            TieredAuditNoticeAdjuster(mutableListOf<Double>(270.0, 4395.0), mutableListOf<Double>(1.02, 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 79891.0,
    )
}
