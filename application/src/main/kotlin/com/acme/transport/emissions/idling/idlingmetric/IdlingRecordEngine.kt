package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.numbers.roundTo

/** One step of the idling record calculation. */
interface IdlingRecordModifier {
    fun describe(): String
    fun adjust(item: IdlingRecord, value: Double): Double
}

data class PercentIdlingRecordAdjuster(var percent: Double) : IdlingRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelIdlingRecordAdjuster(var factors: MutableMap<String, Double>) : IdlingRecordModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: IdlingRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingIdlingRecordAdjuster(var step: Double) : IdlingRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingRecord, value: Double): Double = roundTo(value, step)
}

data class MinimumIdlingRecordAdjuster(var minimum: Double) : IdlingRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapIdlingRecordAdjuster(var cap: Double) : IdlingRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingRecord, value: Double): Double = minOf(value, cap)
}

data class IdlingRecordDistanceKmOffsetAdjuster(var weight: Double) : IdlingRecordModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: IdlingRecord, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the idling record steps in order and keeps a trace of each. */
data class IdlingRecordEngine(
    var steps: MutableList<IdlingRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingRecord): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingRecordEngine(): IdlingRecordEngine {
    return IdlingRecordEngine(
        steps = mutableListOf<IdlingRecordModifier>(
            PercentIdlingRecordAdjuster(20.5),
            ChannelIdlingRecordAdjuster(mutableMapOf<String, Double>("central" to 0.72, "north" to 1.23)),
            RoundingIdlingRecordAdjuster(0.05),
            MinimumIdlingRecordAdjuster(42.6),
            CapIdlingRecordAdjuster(40517.0),
            IdlingRecordDistanceKmOffsetAdjuster(0.37),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 73851.0,
    )
}
