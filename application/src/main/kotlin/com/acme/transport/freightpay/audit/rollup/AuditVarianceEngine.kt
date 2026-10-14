package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the audit variance calculation. */
interface AuditVarianceStep {
    fun describe(): String
    fun adjust(item: AuditVariance, value: Double): Double
}

data class FlaggedAuditVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAuditVarianceAdjuster(var step: Double) : AuditVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditVariance, value: Double): Double = roundTo(value, step)
}

data class CapAuditVarianceAdjuster(var cap: Double) : AuditVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the audit variance steps in order and keeps a trace of each. */
data class AuditVarianceEngine(
    var steps: MutableList<AuditVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AuditVariance): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAuditVarianceEngine(flags: Registry): AuditVarianceEngine {
    return AuditVarianceEngine(
        steps = mutableListOf<AuditVarianceStep>(
            FlaggedAuditVarianceAdjuster(flags, "audit-variance-default", -4.1),
            RoundingAuditVarianceAdjuster(0.1),
            CapAuditVarianceAdjuster(22051.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 67148.9,
    )
}
