package com.acme.transport.customs.inspection.notice

import com.acme.transport.kit.flags.Registry

/** One step of the pending inspection notice calculation. */
interface PendingInspectionNoticeFactor {
    fun describe(): String
    fun adjust(item: PendingInspectionNotice, value: Double): Double
}

data class PendingInspectionNoticePalletsOffsetAdjuster(var weight: Double) : PendingInspectionNoticeFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: PendingInspectionNotice, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class FlaggedPendingInspectionNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingInspectionNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingInspectionNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingInspectionNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingInspectionNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingInspectionNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingInspectionNoticeAdjuster(var percent: Double) : PendingInspectionNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingInspectionNoticeAdjuster(var cap: Double) : PendingInspectionNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingInspectionNotice, value: Double): Double = minOf(value, cap)
}

/** Runs the pending inspection notice steps in order and keeps a trace of each. */
data class PendingInspectionNoticeEngine(
    var steps: MutableList<PendingInspectionNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingInspectionNotice): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingInspectionNoticeEngine(flags: Registry): PendingInspectionNoticeEngine {
    return PendingInspectionNoticeEngine(
        steps = mutableListOf<PendingInspectionNoticeFactor>(
            PendingInspectionNoticePalletsOffsetAdjuster(1.61),
            FlaggedPendingInspectionNoticeAdjuster(flags, "pending-inspection-notice-scheduled", -6.9),
            TieredPendingInspectionNoticeAdjuster(
                mutableListOf<Double>(6487.0, 8298.0),
                mutableListOf<Double>(1.04, 0.95),
            ),
            PercentPendingInspectionNoticeAdjuster(24.7),
            CapPendingInspectionNoticeAdjuster(15258.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 51665.0,
    )
}
