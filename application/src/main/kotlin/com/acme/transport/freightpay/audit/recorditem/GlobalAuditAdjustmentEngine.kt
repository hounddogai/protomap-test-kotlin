package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global audit adjustment calculation. */
interface GlobalAuditAdjustmentFactor {
    fun describe(): String
    fun adjust(item: GlobalAuditAdjustment, value: Double): Double
}

data class MinimumGlobalAuditAdjustmentAdjuster(var minimum: Double) : GlobalAuditAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalAuditAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalAuditAdjustmentAdjuster(var step: Double) : GlobalAuditAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAuditAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalAuditAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAuditAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAuditAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalAuditAdjustmentAdjuster(var cap: Double) : GlobalAuditAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAuditAdjustment, value: Double): Double = minOf(value, cap)
}

data class TitleGlobalAuditAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalAuditAdjustmentFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalAuditAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global audit adjustment steps in order and keeps a trace of each. */
data class GlobalAuditAdjustmentEngine(
    var steps: MutableList<GlobalAuditAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalAuditAdjustment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAuditAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalAuditAdjustmentEngine(flags: Registry): GlobalAuditAdjustmentEngine {
    return GlobalAuditAdjustmentEngine(
        steps = mutableListOf<GlobalAuditAdjustmentFactor>(
            MinimumGlobalAuditAdjustmentAdjuster(37.4),
            RoundingGlobalAuditAdjustmentAdjuster(0.5),
            FlaggedGlobalAuditAdjustmentAdjuster(flags, "global-audit-adjustment-legacy", 3.6),
            CapGlobalAuditAdjustmentAdjuster(34206.0),
            TitleGlobalAuditAdjustmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.15, "east" to 0.96, "north" to 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 78937.8,
    )
}
