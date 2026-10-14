package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted audit claim calculation. */
interface WeightedAuditClaimStep {
    fun describe(): String
    fun adjust(item: WeightedAuditClaim, value: Double): Double
}

data class RoundingWeightedAuditClaimAdjuster(var step: Double) : WeightedAuditClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAuditClaim, value: Double): Double = roundTo(value, step)
}

data class CapWeightedAuditClaimAdjuster(var cap: Double) : WeightedAuditClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAuditClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedAuditClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAuditClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAuditClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedAuditClaimMinimumOffsetAdjuster(var weight: Double) : WeightedAuditClaimStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: WeightedAuditClaim, value: Double): Double = value + item.minimum * weight
}

/** Runs the weighted audit claim steps in order and keeps a trace of each. */
data class WeightedAuditClaimEngine(
    var steps: MutableList<WeightedAuditClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedAuditClaim): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAuditClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAuditClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedAuditClaimEngine(flags: Registry): WeightedAuditClaimEngine {
    return WeightedAuditClaimEngine(
        steps = mutableListOf<WeightedAuditClaimStep>(
            RoundingWeightedAuditClaimAdjuster(0.25),
            CapWeightedAuditClaimAdjuster(21971.0),
            FlaggedWeightedAuditClaimAdjuster(flags, "weighted-audit-claim-scheduled", 1.6),
            WeightedAuditClaimMinimumOffsetAdjuster(1.51),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 68193.5,
    )
}
