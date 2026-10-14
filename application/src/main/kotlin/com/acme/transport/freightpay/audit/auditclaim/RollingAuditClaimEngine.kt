package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling audit claim calculation. */
interface RollingAuditClaimFactor {
    fun describe(): String
    fun adjust(item: RollingAuditClaim, value: Double): Double
}

data class TieredRollingAuditClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingAuditClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingAuditClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingAuditClaimAdjuster(var cap: Double) : RollingAuditClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAuditClaim, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingAuditClaimAdjuster(var step: Double) : RollingAuditClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAuditClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingAuditClaimAdjuster(var minimum: Double) : RollingAuditClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingAuditClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling audit claim steps in order and keeps a trace of each. */
data class RollingAuditClaimEngine(
    var steps: MutableList<RollingAuditClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingAuditClaim): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingAuditClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingAuditClaimEngine(): RollingAuditClaimEngine {
    return RollingAuditClaimEngine(
        steps = mutableListOf<RollingAuditClaimFactor>(
            TieredRollingAuditClaimAdjuster(
                mutableListOf<Double>(1272.0, 2201.0, 7129.0, 8762.0),
                mutableListOf<Double>(0.89, 1.2, 0.82, 1.01),
            ),
            CapRollingAuditClaimAdjuster(34135.0),
            RoundingRollingAuditClaimAdjuster(1.0),
            MinimumRollingAuditClaimAdjuster(12.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 12138.2,
    )
}
