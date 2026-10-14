package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy clearance notice calculation. */
interface LegacyClearanceNoticeStep {
    fun describe(): String
    fun adjust(item: LegacyClearanceNotice, value: Double): Double
}

data class LegacyClearanceNoticeExpiresAtOffsetAdjuster(var weight: Double) : LegacyClearanceNoticeStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: LegacyClearanceNotice, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class FlaggedLegacyClearanceNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyClearanceNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyClearanceNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyClearanceNoticeAdjuster(var cap: Double) : LegacyClearanceNoticeStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyClearanceNotice, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyClearanceNoticeAdjuster(var step: Double) : LegacyClearanceNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyClearanceNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy clearance notice steps in order and keeps a trace of each. */
data class LegacyClearanceNoticeEngine(
    var steps: MutableList<LegacyClearanceNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyClearanceNotice): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyClearanceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyClearanceNoticeEngine(flags: Registry): LegacyClearanceNoticeEngine {
    return LegacyClearanceNoticeEngine(
        steps = mutableListOf<LegacyClearanceNoticeStep>(
            LegacyClearanceNoticeExpiresAtOffsetAdjuster(1.01),
            FlaggedLegacyClearanceNoticeAdjuster(flags, "legacy-clearance-notice-primary", -5.8),
            CapLegacyClearanceNoticeAdjuster(49802.0),
            RoundingLegacyClearanceNoticeAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 19419.3,
    )
}
