package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling profile calculation. */
interface IdlingProfileModifier {
    fun describe(): String
    fun adjust(item: IdlingProfile, value: Double): Double
}

data class MinimumIdlingProfileAdjuster(var minimum: Double) : IdlingProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredIdlingProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingIdlingProfileAdjuster(var step: Double) : IdlingProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingProfile, value: Double): Double = roundTo(value, step)
}

data class IdlingProfileCeilingOffsetAdjuster(var weight: Double) : IdlingProfileModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: IdlingProfile, value: Double): Double = value + item.ceiling * weight
}

data class PercentIdlingProfileAdjuster(var percent: Double) : IdlingProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelIdlingProfileAdjuster(var factors: MutableMap<String, Double>) : IdlingProfileModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: IdlingProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the idling profile steps in order and keeps a trace of each. */
data class IdlingProfileEngine(
    var steps: MutableList<IdlingProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingProfile): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingProfileEngine(): IdlingProfileEngine {
    return IdlingProfileEngine(
        steps = mutableListOf<IdlingProfileModifier>(
            MinimumIdlingProfileAdjuster(1.4),
            TieredIdlingProfileAdjuster(mutableListOf<Double>(746.0, 4344.0), mutableListOf<Double>(0.83, 0.99)),
            RoundingIdlingProfileAdjuster(0.05),
            IdlingProfileCeilingOffsetAdjuster(1.94),
            PercentIdlingProfileAdjuster(10.0),
            LabelIdlingProfileAdjuster(mutableMapOf<String, Double>("central" to 1.34, "north" to 0.79)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 48591.9,
    )
}
