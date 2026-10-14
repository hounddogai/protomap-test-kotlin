package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling adjustment calculation. */
interface IdlingAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: IdlingAdjustment, value: Double): Double
}

data class PercentIdlingAdjustmentAdjuster(var percent: Double) : IdlingAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumIdlingAdjustmentAdjuster(var minimum: Double) : IdlingAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapIdlingAdjustmentAdjuster(var cap: Double) : IdlingAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedIdlingAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleIdlingAdjustmentAdjuster(var factors: MutableMap<String, Double>) : IdlingAdjustmentAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: IdlingAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class IdlingAdjustmentDurationMinutesOffsetAdjuster(var weight: Double) : IdlingAdjustmentAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: IdlingAdjustment, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the idling adjustment steps in order and keeps a trace of each. */
data class IdlingAdjustmentEngine(
    var steps: MutableList<IdlingAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingAdjustment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingAdjustmentEngine(flags: Registry): IdlingAdjustmentEngine {
    return IdlingAdjustmentEngine(
        steps = mutableListOf<IdlingAdjustmentAdjuster>(
            PercentIdlingAdjustmentAdjuster(-12.7),
            MinimumIdlingAdjustmentAdjuster(8.7),
            CapIdlingAdjustmentAdjuster(20069.0),
            FlaggedIdlingAdjustmentAdjuster(flags, "idling-adjustment-weighted", 7.3),
            LocaleIdlingAdjustmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.29, "central" to 1.03, "west" to 0.89),
            ),
            IdlingAdjustmentDurationMinutesOffsetAdjuster(0.71),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 52597.0,
    )
}
