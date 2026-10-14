package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the duty period draft calculation. */
interface DutyPeriodDraftModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodDraft, value: Double): Double
}

data class DutyPeriodDraftGraceDaysOffsetAdjuster(var weight: Double) : DutyPeriodDraftModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DutyPeriodDraft, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class FlaggedDutyPeriodDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDutyPeriodDraftAdjuster(var cap: Double) : DutyPeriodDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the duty period draft steps in order and keeps a trace of each. */
data class DutyPeriodDraftEngine(
    var steps: MutableList<DutyPeriodDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DutyPeriodDraft): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDutyPeriodDraftEngine(flags: Registry): DutyPeriodDraftEngine {
    return DutyPeriodDraftEngine(
        steps = mutableListOf<DutyPeriodDraftModifier>(
            DutyPeriodDraftGraceDaysOffsetAdjuster(0.99),
            FlaggedDutyPeriodDraftAdjuster(flags, "duty-period-draft-primary", -4.3),
            CapDutyPeriodDraftAdjuster(13838.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 39205.3,
    )
}
