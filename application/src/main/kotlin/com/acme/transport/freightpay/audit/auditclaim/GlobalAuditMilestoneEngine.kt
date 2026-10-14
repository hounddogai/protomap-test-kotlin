package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global audit milestone calculation. */
interface GlobalAuditMilestoneModifier {
    fun describe(): String
    fun adjust(item: GlobalAuditMilestone, value: Double): Double
}

data class GlobalAuditMilestoneBatchSizeOffsetAdjuster(var weight: Double) : GlobalAuditMilestoneModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: GlobalAuditMilestone, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class RoundingGlobalAuditMilestoneAdjuster(var step: Double) : GlobalAuditMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAuditMilestone, value: Double): Double = roundTo(value, step)
}

data class CapGlobalAuditMilestoneAdjuster(var cap: Double) : GlobalAuditMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAuditMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalAuditMilestoneAdjuster(var minimum: Double) : GlobalAuditMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalAuditMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global audit milestone steps in order and keeps a trace of each. */
data class GlobalAuditMilestoneEngine(
    var steps: MutableList<GlobalAuditMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalAuditMilestone): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAuditMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalAuditMilestoneEngine(): GlobalAuditMilestoneEngine {
    return GlobalAuditMilestoneEngine(
        steps = mutableListOf<GlobalAuditMilestoneModifier>(
            GlobalAuditMilestoneBatchSizeOffsetAdjuster(0.16),
            RoundingGlobalAuditMilestoneAdjuster(1.0),
            CapGlobalAuditMilestoneAdjuster(26510.0),
            MinimumGlobalAuditMilestoneAdjuster(30.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 5834.6,
    )
}
