package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the audit tier calculation. */
interface AuditTierModifier {
    fun describe(): String
    fun adjust(item: AuditTier, value: Double): Double
}

data class PercentAuditTierAdjuster(var percent: Double) : AuditTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAuditTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAuditTierAdjuster(var minimum: Double) : AuditTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAuditTierAdjuster(var cap: Double) : AuditTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditTier, value: Double): Double = minOf(value, cap)
}

data class AuditTierQuantityOffsetAdjuster(var weight: Double) : AuditTierModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AuditTier, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the audit tier steps in order and keeps a trace of each. */
data class AuditTierEngine(
    var steps: MutableList<AuditTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditTier): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AuditTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditTierEngine(): AuditTierEngine {
    return AuditTierEngine(
        steps = mutableListOf<AuditTierModifier>(
            PercentAuditTierAdjuster(-10.3),
            TieredAuditTierAdjuster(
                mutableListOf<Double>(4176.0, 7223.0, 7747.0),
                mutableListOf<Double>(0.82, 1.14, 1.11),
            ),
            MinimumAuditTierAdjuster(5.0),
            CapAuditTierAdjuster(31371.0),
            AuditTierQuantityOffsetAdjuster(0.19),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 93244.3,
    )
}
