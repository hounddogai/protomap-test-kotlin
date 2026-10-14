package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.Registry

/** One step of the regional audit slot calculation. */
interface RegionalAuditSlotFactor {
    fun describe(): String
    fun adjust(item: RegionalAuditSlot, value: Double): Double
}

data class TitleRegionalAuditSlotAdjuster(var factors: MutableMap<String, Double>) : RegionalAuditSlotFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalAuditSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalAuditSlotOdometerOffsetAdjuster(var weight: Double) : RegionalAuditSlotFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RegionalAuditSlot, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class FlaggedRegionalAuditSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAuditSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAuditSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional audit slot steps in order and keeps a trace of each. */
data class RegionalAuditSlotEngine(
    var steps: MutableList<RegionalAuditSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalAuditSlot): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalAuditSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalAuditSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalAuditSlotEngine(flags: Registry): RegionalAuditSlotEngine {
    return RegionalAuditSlotEngine(
        steps = mutableListOf<RegionalAuditSlotFactor>(
            TitleRegionalAuditSlotAdjuster(mutableMapOf<String, Double>("east" to 0.86, "west" to 1.12)),
            RegionalAuditSlotOdometerOffsetAdjuster(1.03),
            FlaggedRegionalAuditSlotAdjuster(flags, "regional-audit-slot-scheduled", -9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 65814.5,
    )
}
