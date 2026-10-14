package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled redelivery entry calculation. */
interface ScheduledRedeliveryEntryModifier {
    fun describe(): String
    fun adjust(item: ScheduledRedeliveryEntry, value: Double): Double
}

data class TieredScheduledRedeliveryEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRedeliveryEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRedeliveryEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledRedeliveryEntryAdjuster(var percent: Double) : ScheduledRedeliveryEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRedeliveryEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledRedeliveryEntryAdjuster(var cap: Double) : ScheduledRedeliveryEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRedeliveryEntry, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledRedeliveryEntryAdjuster(var minimum: Double) : ScheduledRedeliveryEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRedeliveryEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled redelivery entry steps in order and keeps a trace of each. */
data class ScheduledRedeliveryEntryEngine(
    var steps: MutableList<ScheduledRedeliveryEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRedeliveryEntry): Double {
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

    fun evaluateAll(items: MutableList<ScheduledRedeliveryEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRedeliveryEntryEngine(): ScheduledRedeliveryEntryEngine {
    return ScheduledRedeliveryEntryEngine(
        steps = mutableListOf<ScheduledRedeliveryEntryModifier>(
            TieredScheduledRedeliveryEntryAdjuster(
                mutableListOf<Double>(417.0, 2827.0, 3648.0, 8256.0),
                mutableListOf<Double>(0.83, 1.19, 0.96, 0.95),
            ),
            PercentScheduledRedeliveryEntryAdjuster(-17.2),
            CapScheduledRedeliveryEntryAdjuster(29995.0),
            MinimumScheduledRedeliveryEntryAdjuster(30.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 35365.0,
    )
}
