package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped audit target calculation. */
interface CappedAuditTargetAdjuster {
    fun describe(): String
    fun adjust(item: CappedAuditTarget, value: Double): Double
}

data class CapCappedAuditTargetAdjuster(var cap: Double) : CappedAuditTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAuditTarget, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedAuditTargetAdjuster(var step: Double) : CappedAuditTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAuditTarget, value: Double): Double = roundTo(value, step)
}

data class NameCappedAuditTargetAdjuster(var factors: MutableMap<String, Double>) : CappedAuditTargetAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedAuditTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedAuditTargetExpiresAtOffsetAdjuster(var weight: Double) : CappedAuditTargetAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CappedAuditTarget, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumCappedAuditTargetAdjuster(var minimum: Double) : CappedAuditTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedAuditTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedAuditTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAuditTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAuditTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped audit target steps in order and keeps a trace of each. */
data class CappedAuditTargetEngine(
    var steps: MutableList<CappedAuditTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedAuditTarget): Double {
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

    fun evaluateAll(items: MutableList<CappedAuditTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedAuditTargetEngine(): CappedAuditTargetEngine {
    return CappedAuditTargetEngine(
        steps = mutableListOf<CappedAuditTargetAdjuster>(
            CapCappedAuditTargetAdjuster(34920.0),
            RoundingCappedAuditTargetAdjuster(0.1),
            NameCappedAuditTargetAdjuster(mutableMapOf<String, Double>("central" to 1.27, "east" to 1.36)),
            CappedAuditTargetExpiresAtOffsetAdjuster(1.57),
            MinimumCappedAuditTargetAdjuster(41.0),
            TieredCappedAuditTargetAdjuster(
                mutableListOf<Double>(1534.0, 8606.0),
                mutableListOf<Double>(1.18, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 19097.7,
    )
}
