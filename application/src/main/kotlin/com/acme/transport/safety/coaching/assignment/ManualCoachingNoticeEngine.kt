package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual coaching notice calculation. */
interface ManualCoachingNoticeModifier {
    fun describe(): String
    fun adjust(item: ManualCoachingNotice, value: Double): Double
}

data class RoundingManualCoachingNoticeAdjuster(var step: Double) : ManualCoachingNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCoachingNotice, value: Double): Double = roundTo(value, step)
}

data class CapManualCoachingNoticeAdjuster(var cap: Double) : ManualCoachingNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCoachingNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualCoachingNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCoachingNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCoachingNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual coaching notice steps in order and keeps a trace of each. */
data class ManualCoachingNoticeEngine(
    var steps: MutableList<ManualCoachingNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualCoachingNotice): Double {
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

    fun evaluateAll(items: MutableList<ManualCoachingNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualCoachingNoticeEngine(flags: Registry): ManualCoachingNoticeEngine {
    return ManualCoachingNoticeEngine(
        steps = mutableListOf<ManualCoachingNoticeModifier>(
            RoundingManualCoachingNoticeAdjuster(0.05),
            CapManualCoachingNoticeAdjuster(22566.0),
            FlaggedManualCoachingNoticeAdjuster(flags, "manual-coaching-notice-legacy", -9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 67054.5,
    )
}
