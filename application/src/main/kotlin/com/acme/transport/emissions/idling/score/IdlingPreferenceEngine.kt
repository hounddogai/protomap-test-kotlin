package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the idling preference calculation. */
interface IdlingPreferenceModifier {
    fun describe(): String
    fun adjust(item: IdlingPreference, value: Double): Double
}

data class LocaleIdlingPreferenceAdjuster(var factors: MutableMap<String, Double>) : IdlingPreferenceModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: IdlingPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentIdlingPreferenceAdjuster(var percent: Double) : IdlingPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIdlingPreferenceAdjuster(var cap: Double) : IdlingPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingPreference, value: Double): Double = minOf(value, cap)
}

data class MinimumIdlingPreferenceAdjuster(var minimum: Double) : IdlingPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class IdlingPreferenceCreatedAtOffsetAdjuster(var weight: Double) : IdlingPreferenceModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: IdlingPreference, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class RoundingIdlingPreferenceAdjuster(var step: Double) : IdlingPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the idling preference steps in order and keeps a trace of each. */
data class IdlingPreferenceEngine(
    var steps: MutableList<IdlingPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingPreferenceEngine(): IdlingPreferenceEngine {
    return IdlingPreferenceEngine(
        steps = mutableListOf<IdlingPreferenceModifier>(
            LocaleIdlingPreferenceAdjuster(mutableMapOf<String, Double>("west" to 0.87, "north" to 1.02)),
            PercentIdlingPreferenceAdjuster(8.2),
            CapIdlingPreferenceAdjuster(23368.0),
            MinimumIdlingPreferenceAdjuster(33.0),
            IdlingPreferenceCreatedAtOffsetAdjuster(0.97),
            RoundingIdlingPreferenceAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 61957.3,
    )
}
