package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional lease return audit calculation. */
interface RegionalLeaseReturnAuditModifier {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnAudit, value: Double): Double
}

data class LaneRegionalLeaseReturnAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalLeaseReturnAuditModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: RegionalLeaseReturnAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalLeaseReturnAuditAdjuster(var percent: Double) : RegionalLeaseReturnAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalLeaseReturnAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRegionalLeaseReturnAuditAdjuster(var cap: Double) : RegionalLeaseReturnAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalLeaseReturnAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalLeaseReturnAuditAdjuster(var step: Double) : RegionalLeaseReturnAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalLeaseReturnAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalLeaseReturnAuditAdjuster(var minimum: Double) : RegionalLeaseReturnAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional lease return audit steps in order and keeps a trace of each. */
data class RegionalLeaseReturnAuditEngine(
    var steps: MutableList<RegionalLeaseReturnAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalLeaseReturnAudit): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalLeaseReturnAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalLeaseReturnAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnAuditEngine(): RegionalLeaseReturnAuditEngine {
    return RegionalLeaseReturnAuditEngine(
        steps = mutableListOf<RegionalLeaseReturnAuditModifier>(
            LaneRegionalLeaseReturnAuditAdjuster(mutableMapOf<String, Double>("east" to 0.96)),
            PercentRegionalLeaseReturnAuditAdjuster(-23.9),
            CapRegionalLeaseReturnAuditAdjuster(8821.0),
            RoundingRegionalLeaseReturnAuditAdjuster(5.0),
            MinimumRegionalLeaseReturnAuditAdjuster(45.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 19168.9,
    )
}
