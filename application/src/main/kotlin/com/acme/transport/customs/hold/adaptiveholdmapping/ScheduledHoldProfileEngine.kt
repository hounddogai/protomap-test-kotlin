package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled hold profile calculation. */
interface ScheduledHoldProfileModifier {
    fun describe(): String
    fun adjust(item: ScheduledHoldProfile, value: Double): Double
}

data class FlaggedScheduledHoldProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHoldProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHoldProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledHoldProfileAdjuster(var step: Double) : ScheduledHoldProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledHoldProfile, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledHoldProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledHoldProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledHoldProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScheduledHoldProfileOdometerOffsetAdjuster(var weight: Double) : ScheduledHoldProfileModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: ScheduledHoldProfile, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the scheduled hold profile steps in order and keeps a trace of each. */
data class ScheduledHoldProfileEngine(
    var steps: MutableList<ScheduledHoldProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledHoldProfile): Double {
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

    fun evaluateAll(items: MutableList<ScheduledHoldProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledHoldProfileEngine(flags: Registry): ScheduledHoldProfileEngine {
    return ScheduledHoldProfileEngine(
        steps = mutableListOf<ScheduledHoldProfileModifier>(
            FlaggedScheduledHoldProfileAdjuster(flags, "scheduled-hold-profile-fallback", -8.2),
            RoundingScheduledHoldProfileAdjuster(0.5),
            TieredScheduledHoldProfileAdjuster(
                mutableListOf<Double>(6908.0, 8735.0),
                mutableListOf<Double>(1.0, 0.88),
            ),
            ScheduledHoldProfileOdometerOffsetAdjuster(0.48),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 28481.4,
    )
}
