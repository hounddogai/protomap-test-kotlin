package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual duty period notice calculation. */
interface ManualDutyPeriodNoticeModifier {
    fun describe(): String
    fun adjust(item: ManualDutyPeriodNotice, value: Double): Double
}

data class RoundingManualDutyPeriodNoticeAdjuster(var step: Double) : ManualDutyPeriodNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDutyPeriodNotice, value: Double): Double = roundTo(value, step)
}

data class ChannelManualDutyPeriodNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDutyPeriodNoticeModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ManualDutyPeriodNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualDutyPeriodNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDutyPeriodNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDutyPeriodNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualDutyPeriodNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDutyPeriodNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDutyPeriodNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualDutyPeriodNoticeAdjuster(var cap: Double) : ManualDutyPeriodNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDutyPeriodNotice, value: Double): Double = minOf(value, cap)
}

/** Runs the manual duty period notice steps in order and keeps a trace of each. */
data class ManualDutyPeriodNoticeEngine(
    var steps: MutableList<ManualDutyPeriodNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDutyPeriodNotice): Double {
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

    fun evaluateAll(items: MutableList<ManualDutyPeriodNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDutyPeriodNoticeEngine(flags: Registry): ManualDutyPeriodNoticeEngine {
    return ManualDutyPeriodNoticeEngine(
        steps = mutableListOf<ManualDutyPeriodNoticeModifier>(
            RoundingManualDutyPeriodNoticeAdjuster(0.5),
            ChannelManualDutyPeriodNoticeAdjuster(
                mutableMapOf<String, Double>("west" to 0.89, "south" to 0.77, "online" to 0.87),
            ),
            TieredManualDutyPeriodNoticeAdjuster(
                mutableListOf<Double>(4732.0, 7116.0, 8498.0),
                mutableListOf<Double>(1.04, 1.18, 1.0),
            ),
            FlaggedManualDutyPeriodNoticeAdjuster(flags, "manual-duty-period-notice-strict", -4.1),
            CapManualDutyPeriodNoticeAdjuster(13763.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 6540.5,
    )
}
