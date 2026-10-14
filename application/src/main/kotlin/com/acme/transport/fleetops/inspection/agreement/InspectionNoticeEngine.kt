package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection notice calculation. */
interface InspectionNoticeFactor {
    fun describe(): String
    fun adjust(item: InspectionNotice, value: Double): Double
}

data class PercentInspectionNoticeAdjuster(var percent: Double) : InspectionNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingInspectionNoticeAdjuster(var step: Double) : InspectionNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionNotice, value: Double): Double = roundTo(value, step)
}

data class CapInspectionNoticeAdjuster(var cap: Double) : InspectionNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedInspectionNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitInspectionNoticeAdjuster(var factors: MutableMap<String, Double>) : InspectionNoticeFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: InspectionNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionNoticeFactor {
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

/** Runs the inspection notice steps in order and keeps a trace of each. */
data class InspectionNoticeEngine(
    var steps: MutableList<InspectionNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionNotice): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionNoticeEngine(flags: Registry): InspectionNoticeEngine {
    return InspectionNoticeEngine(
        steps = mutableListOf<InspectionNoticeFactor>(
            PercentInspectionNoticeAdjuster(21.2),
            RoundingInspectionNoticeAdjuster(1.0),
            CapInspectionNoticeAdjuster(17347.0),
            FlaggedInspectionNoticeAdjuster(flags, "inspection-notice-seasonal", -2.0),
            UnitInspectionNoticeAdjuster(mutableMapOf<String, Double>("east" to 1.29, "online" to 0.88)),
            TieredInspectionNoticeAdjuster(
                mutableListOf<Double>(6439.0, 8037.0),
                mutableListOf<Double>(1.04, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 75787.7,
    )
}
