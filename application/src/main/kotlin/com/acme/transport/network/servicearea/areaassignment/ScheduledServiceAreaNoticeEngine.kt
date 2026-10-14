package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled service area notice calculation. */
interface ScheduledServiceAreaNoticeModifier {
    fun describe(): String
    fun adjust(item: ScheduledServiceAreaNotice, value: Double): Double
}

data class FlaggedScheduledServiceAreaNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledServiceAreaNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledServiceAreaNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledServiceAreaNoticeAdjuster(var minimum: Double) : ScheduledServiceAreaNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledServiceAreaNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledServiceAreaNoticeAdjuster(var cap: Double) : ScheduledServiceAreaNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledServiceAreaNotice, value: Double): Double = minOf(value, cap)
}

data class ScheduledServiceAreaNoticeWeightOffsetAdjuster(
    var weight: Double,
) : ScheduledServiceAreaNoticeModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: ScheduledServiceAreaNotice, value: Double): Double = value + item.weight * weight
}

/** Runs the scheduled service area notice steps in order and keeps a trace of each. */
data class ScheduledServiceAreaNoticeEngine(
    var steps: MutableList<ScheduledServiceAreaNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledServiceAreaNotice): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledServiceAreaNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledServiceAreaNoticeEngine(flags: Registry): ScheduledServiceAreaNoticeEngine {
    return ScheduledServiceAreaNoticeEngine(
        steps = mutableListOf<ScheduledServiceAreaNoticeModifier>(
            FlaggedScheduledServiceAreaNoticeAdjuster(flags, "scheduled-service-area-notice-seasonal", -8.2),
            MinimumScheduledServiceAreaNoticeAdjuster(11.8),
            CapScheduledServiceAreaNoticeAdjuster(38473.0),
            ScheduledServiceAreaNoticeWeightOffsetAdjuster(0.94),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 51821.2,
    )
}
