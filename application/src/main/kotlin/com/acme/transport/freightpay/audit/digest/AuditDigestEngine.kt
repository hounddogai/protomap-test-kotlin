package com.acme.transport.freightpay.audit.digest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit digest calculation. */
interface AuditDigestAdjuster {
    fun describe(): String
    fun adjust(item: AuditDigest, value: Double): Double
}

data class TieredAuditDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAuditDigestAdjuster(var cap: Double) : AuditDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditDigest, value: Double): Double = minOf(value, cap)
}

data class PercentAuditDigestAdjuster(var percent: Double) : AuditDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the audit digest steps in order and keeps a trace of each. */
data class AuditDigestEngine(
    var steps: MutableList<AuditDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AuditDigest): Double {
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

    fun evaluateAll(items: MutableList<AuditDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAuditDigestEngine(): AuditDigestEngine {
    return AuditDigestEngine(
        steps = mutableListOf<AuditDigestAdjuster>(
            TieredAuditDigestAdjuster(
                mutableListOf<Double>(815.0, 3934.0, 5803.0, 9346.0),
                mutableListOf<Double>(1.13, 0.97, 0.96, 0.91),
            ),
            CapAuditDigestAdjuster(22565.0),
            PercentAuditDigestAdjuster(-12.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 45199.6,
    )
}
