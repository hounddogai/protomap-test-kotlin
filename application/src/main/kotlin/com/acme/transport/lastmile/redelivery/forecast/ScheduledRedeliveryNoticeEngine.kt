package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled redelivery notice calculation. */
interface ScheduledRedeliveryNoticeModifier {
    fun describe(): String
    fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double
}

data class TieredScheduledRedeliveryNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRedeliveryNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledRedeliveryNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRedeliveryNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledRedeliveryNoticeAdjuster(var step: Double) : ScheduledRedeliveryNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledRedeliveryNoticeAdjuster(var minimum: Double) : ScheduledRedeliveryNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledRedeliveryNoticeAdjuster(var cap: Double) : ScheduledRedeliveryNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledRedeliveryNoticeAdjuster(var percent: Double) : ScheduledRedeliveryNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRedeliveryNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled redelivery notice steps in order and keeps a trace of each. */
data class ScheduledRedeliveryNoticeEngine(
    var steps: MutableList<ScheduledRedeliveryNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledRedeliveryNotice): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRedeliveryNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledRedeliveryNoticeEngine(flags: Registry): ScheduledRedeliveryNoticeEngine {
    return ScheduledRedeliveryNoticeEngine(
        steps = mutableListOf<ScheduledRedeliveryNoticeModifier>(
            TieredScheduledRedeliveryNoticeAdjuster(
                mutableListOf<Double>(182.0, 433.0),
                mutableListOf<Double>(0.91, 0.95),
            ),
            FlaggedScheduledRedeliveryNoticeAdjuster(flags, "scheduled-redelivery-notice-scheduled", 5.9),
            RoundingScheduledRedeliveryNoticeAdjuster(0.05),
            MinimumScheduledRedeliveryNoticeAdjuster(23.7),
            CapScheduledRedeliveryNoticeAdjuster(24121.0),
            PercentScheduledRedeliveryNoticeAdjuster(17.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 69619.9,
    )
}
