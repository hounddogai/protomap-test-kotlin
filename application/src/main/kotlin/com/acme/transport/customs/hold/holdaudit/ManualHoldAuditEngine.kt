package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual hold audit calculation. */
interface ManualHoldAuditStep {
    fun describe(): String
    fun adjust(item: ManualHoldAudit, value: Double): Double
}

data class FlaggedManualHoldAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualHoldAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualHoldAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualHoldAuditAdjuster(var step: Double) : ManualHoldAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualHoldAudit, value: Double): Double = roundTo(value, step)
}

data class CategoryManualHoldAuditAdjuster(var factors: MutableMap<String, Double>) : ManualHoldAuditStep {
    override fun describe(): String = "by category"

    override fun adjust(item: ManualHoldAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual hold audit steps in order and keeps a trace of each. */
data class ManualHoldAuditEngine(
    var steps: MutableList<ManualHoldAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualHoldAudit): Double {
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

    fun evaluateAll(items: MutableList<ManualHoldAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualHoldAuditEngine(flags: Registry): ManualHoldAuditEngine {
    return ManualHoldAuditEngine(
        steps = mutableListOf<ManualHoldAuditStep>(
            FlaggedManualHoldAuditAdjuster(flags, "manual-hold-audit-pending", 8.4),
            RoundingManualHoldAuditAdjuster(0.05),
            CategoryManualHoldAuditAdjuster(
                mutableMapOf<String, Double>("central" to 0.8, "north" to 1.09, "retail" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 92317.9,
    )
}
