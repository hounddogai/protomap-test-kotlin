package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling backhaul audit calculation. */
interface RollingBackhaulAuditModifier {
    fun describe(): String
    fun adjust(item: RollingBackhaulAudit, value: Double): Double
}

data class PercentRollingBackhaulAuditAdjuster(var percent: Double) : RollingBackhaulAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingBackhaulAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingBackhaulAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingBackhaulAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingBackhaulAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingBackhaulAuditAdjuster(var minimum: Double) : RollingBackhaulAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingBackhaulAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingBackhaulAuditAdjuster(var cap: Double) : RollingBackhaulAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingBackhaulAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling backhaul audit steps in order and keeps a trace of each. */
data class RollingBackhaulAuditEngine(
    var steps: MutableList<RollingBackhaulAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingBackhaulAudit): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingBackhaulAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingBackhaulAuditEngine(): RollingBackhaulAuditEngine {
    return RollingBackhaulAuditEngine(
        steps = mutableListOf<RollingBackhaulAuditModifier>(
            PercentRollingBackhaulAuditAdjuster(16.8),
            TieredRollingBackhaulAuditAdjuster(
                mutableListOf<Double>(2027.0, 2861.0, 9268.0, 9615.0),
                mutableListOf<Double>(0.94, 1.01, 0.92, 0.99),
            ),
            MinimumRollingBackhaulAuditAdjuster(45.9),
            CapRollingBackhaulAuditAdjuster(14852.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 54478.8,
    )
}
