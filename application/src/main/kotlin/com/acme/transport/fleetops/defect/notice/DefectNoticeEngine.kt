package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect notice calculation. */
interface DefectNoticeStep {
    fun describe(): String
    fun adjust(item: DefectNotice, value: Double): Double
}

data class RoundingDefectNoticeAdjuster(var step: Double) : DefectNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectNotice, value: Double): Double = roundTo(value, step)
}

data class LabelDefectNoticeAdjuster(var factors: MutableMap<String, Double>) : DefectNoticeStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DefectNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefectNoticeMarginOffsetAdjuster(var weight: Double) : DefectNoticeStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DefectNotice, value: Double): Double = value + item.margin * weight
}

data class MinimumDefectNoticeAdjuster(var minimum: Double) : DefectNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefectNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectNoticeStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefectNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the defect notice steps in order and keeps a trace of each. */
data class DefectNoticeEngine(
    var steps: MutableList<DefectNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefectNotice): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefectNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefectNoticeEngine(flags: Registry): DefectNoticeEngine {
    return DefectNoticeEngine(
        steps = mutableListOf<DefectNoticeStep>(
            RoundingDefectNoticeAdjuster(1.0),
            LabelDefectNoticeAdjuster(
                mutableMapOf<String, Double>("south" to 0.93, "central" to 1.12, "retail" to 1.08),
            ),
            DefectNoticeMarginOffsetAdjuster(1.76),
            MinimumDefectNoticeAdjuster(1.5),
            TieredDefectNoticeAdjuster(mutableListOf<Double>(5032.0, 8150.0), mutableListOf<Double>(0.93, 1.08)),
            FlaggedDefectNoticeAdjuster(flags, "defect-notice-strict", -7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 84765.5,
    )
}
