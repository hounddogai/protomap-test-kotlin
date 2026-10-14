package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled qualification notice calculation. */
interface ScheduledQualificationNoticeAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledQualificationNotice, value: Double): Double
}

data class FlaggedScheduledQualificationNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledQualificationNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledQualificationNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledQualificationNoticeAdjuster(
    var percent: Double,
) : ScheduledQualificationNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledQualificationNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScheduledQualificationNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledQualificationNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledQualificationNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledQualificationNoticeAdjuster(
    var step: Double,
) : ScheduledQualificationNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledQualificationNotice, value: Double): Double = roundTo(value, step)
}

data class CategoryScheduledQualificationNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledQualificationNoticeAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledQualificationNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled qualification notice steps in order and keeps a trace of each. */
data class ScheduledQualificationNoticeEngine(
    var steps: MutableList<ScheduledQualificationNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledQualificationNotice): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledQualificationNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledQualificationNoticeEngine(flags: Registry): ScheduledQualificationNoticeEngine {
    return ScheduledQualificationNoticeEngine(
        steps = mutableListOf<ScheduledQualificationNoticeAdjuster>(
            FlaggedScheduledQualificationNoticeAdjuster(flags, "scheduled-qualification-notice-regional", -5.0),
            PercentScheduledQualificationNoticeAdjuster(5.4),
            TieredScheduledQualificationNoticeAdjuster(
                mutableListOf<Double>(6004.0, 6094.0),
                mutableListOf<Double>(0.94, 0.83),
            ),
            RoundingScheduledQualificationNoticeAdjuster(5.0),
            CategoryScheduledQualificationNoticeAdjuster(mutableMapOf<String, Double>("south" to 1.38)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 49115.4,
    )
}
