package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.Registry

/** One step of the fallback service plan notice calculation. */
interface FallbackServicePlanNoticeModifier {
    fun describe(): String
    fun adjust(item: FallbackServicePlanNotice, value: Double): Double
}

data class PercentFallbackServicePlanNoticeAdjuster(var percent: Double) : FallbackServicePlanNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackServicePlanNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackServicePlanNoticeAdjuster(var minimum: Double) : FallbackServicePlanNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackServicePlanNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackServicePlanNoticeDurationMinutesOffsetAdjuster(
    var weight: Double,
) : FallbackServicePlanNoticeModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: FallbackServicePlanNotice, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedFallbackServicePlanNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackServicePlanNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackServicePlanNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackServicePlanNoticeAdjuster(var cap: Double) : FallbackServicePlanNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackServicePlanNotice, value: Double): Double = minOf(value, cap)
}

data class SegmentFallbackServicePlanNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackServicePlanNoticeModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: FallbackServicePlanNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback service plan notice steps in order and keeps a trace of each. */
data class FallbackServicePlanNoticeEngine(
    var steps: MutableList<FallbackServicePlanNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackServicePlanNotice): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackServicePlanNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackServicePlanNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackServicePlanNoticeEngine(flags: Registry): FallbackServicePlanNoticeEngine {
    return FallbackServicePlanNoticeEngine(
        steps = mutableListOf<FallbackServicePlanNoticeModifier>(
            PercentFallbackServicePlanNoticeAdjuster(-22.4),
            MinimumFallbackServicePlanNoticeAdjuster(29.2),
            FallbackServicePlanNoticeDurationMinutesOffsetAdjuster(1.0),
            FlaggedFallbackServicePlanNoticeAdjuster(flags, "fallback-service-plan-notice-default", 5.2),
            CapFallbackServicePlanNoticeAdjuster(46116.0),
            SegmentFallbackServicePlanNoticeAdjuster(
                mutableMapOf<String, Double>("central" to 1.24, "north" to 0.7),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 63562.0,
    )
}
