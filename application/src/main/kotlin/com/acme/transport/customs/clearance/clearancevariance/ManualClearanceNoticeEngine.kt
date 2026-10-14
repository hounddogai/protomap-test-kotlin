package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual clearance notice calculation. */
interface ManualClearanceNoticeAdjuster {
    fun describe(): String
    fun adjust(item: ManualClearanceNotice, value: Double): Double
}

data class PercentManualClearanceNoticeAdjuster(var percent: Double) : ManualClearanceNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualClearanceNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualClearanceNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualClearanceNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualClearanceNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualClearanceNoticeAdjuster(var cap: Double) : ManualClearanceNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualClearanceNotice, value: Double): Double = minOf(value, cap)
}

data class MinimumManualClearanceNoticeAdjuster(var minimum: Double) : ManualClearanceNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualClearanceNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualClearanceNoticeAdjuster(var step: Double) : ManualClearanceNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualClearanceNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the manual clearance notice steps in order and keeps a trace of each. */
data class ManualClearanceNoticeEngine(
    var steps: MutableList<ManualClearanceNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualClearanceNotice): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualClearanceNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualClearanceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualClearanceNoticeEngine(flags: Registry): ManualClearanceNoticeEngine {
    return ManualClearanceNoticeEngine(
        steps = mutableListOf<ManualClearanceNoticeAdjuster>(
            PercentManualClearanceNoticeAdjuster(21.2),
            FlaggedManualClearanceNoticeAdjuster(flags, "manual-clearance-notice-seasonal", 2.4),
            CapManualClearanceNoticeAdjuster(18557.0),
            MinimumManualClearanceNoticeAdjuster(17.0),
            RoundingManualClearanceNoticeAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 62445.7,
    )
}
