package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual audit slot calculation. */
interface ManualAuditSlotFactor {
    fun describe(): String
    fun adjust(item: ManualAuditSlot, value: Double): Double
}

data class RoundingManualAuditSlotAdjuster(var step: Double) : ManualAuditSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAuditSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumManualAuditSlotAdjuster(var minimum: Double) : ManualAuditSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAuditSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualAuditSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAuditSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAuditSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual audit slot steps in order and keeps a trace of each. */
data class ManualAuditSlotEngine(
    var steps: MutableList<ManualAuditSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualAuditSlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualAuditSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualAuditSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualAuditSlotEngine(flags: Registry): ManualAuditSlotEngine {
    return ManualAuditSlotEngine(
        steps = mutableListOf<ManualAuditSlotFactor>(
            RoundingManualAuditSlotAdjuster(0.5),
            MinimumManualAuditSlotAdjuster(17.2),
            FlaggedManualAuditSlotAdjuster(flags, "manual-audit-slot-regional", -5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 70434.5,
    )
}
