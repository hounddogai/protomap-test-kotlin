package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the audit threshold calculation. */
interface AuditThresholdModifier {
    fun describe(): String
    fun adjust(item: AuditThreshold, value: Double): Double
}

data class PercentAuditThresholdAdjuster(var percent: Double) : AuditThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAuditThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAuditThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AuditThresholdLimitOffsetAdjuster(var weight: Double) : AuditThresholdModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: AuditThreshold, value: Double): Double = value + item.limit.toDouble() * weight
}

data class RoundingAuditThresholdAdjuster(var step: Double) : AuditThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditThreshold, value: Double): Double = roundTo(value, step)
}

data class NameAuditThresholdAdjuster(var factors: MutableMap<String, Double>) : AuditThresholdModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AuditThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the audit threshold steps in order and keeps a trace of each. */
data class AuditThresholdEngine(
    var steps: MutableList<AuditThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AuditThreshold): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAuditThresholdEngine(flags: Registry): AuditThresholdEngine {
    return AuditThresholdEngine(
        steps = mutableListOf<AuditThresholdModifier>(
            PercentAuditThresholdAdjuster(7.2),
            FlaggedAuditThresholdAdjuster(flags, "audit-threshold-batched", -5.1),
            TieredAuditThresholdAdjuster(
                mutableListOf<Double>(4062.0, 5773.0),
                mutableListOf<Double>(1.15, 0.87),
            ),
            AuditThresholdLimitOffsetAdjuster(0.99),
            RoundingAuditThresholdAdjuster(0.5),
            NameAuditThresholdAdjuster(mutableMapOf<String, Double>("north" to 1.27)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 70915.2,
    )
}
