package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection notice calculation. */
interface InspectionNoticeModifier {
    fun describe(): String
    fun adjust(item: InspectionNotice, value: Double): Double
}

data class FlaggedInspectionNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionNoticeAdjuster(var cap: Double) : InspectionNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionNotice, value: Double): Double = minOf(value, cap)
}

data class RoundingInspectionNoticeAdjuster(var step: Double) : InspectionNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionNotice, value: Double): Double = roundTo(value, step)
}

data class TieredInspectionNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceInspectionNoticeAdjuster(var factors: MutableMap<String, Double>) : InspectionNoticeModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: InspectionNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection notice steps in order and keeps a trace of each. */
data class InspectionNoticeEngine(
    var steps: MutableList<InspectionNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionNoticeEngine(flags: Registry): InspectionNoticeEngine {
    return InspectionNoticeEngine(
        steps = mutableListOf<InspectionNoticeModifier>(
            FlaggedInspectionNoticeAdjuster(flags, "inspection-notice-fallback", -9.5),
            CapInspectionNoticeAdjuster(3696.0),
            RoundingInspectionNoticeAdjuster(1.0),
            TieredInspectionNoticeAdjuster(
                mutableListOf<Double>(105.0, 8294.0, 9086.0, 9248.0),
                mutableListOf<Double>(0.85, 1.02, 1.03, 1.15),
            ),
            SourceInspectionNoticeAdjuster(mutableMapOf<String, Double>("east" to 0.73)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 31068.0,
    )
}
