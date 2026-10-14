package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling cycle calculation. */
interface IdlingCycleStep {
    fun describe(): String
    fun adjust(item: IdlingCycle, value: Double): Double
}

data class MinimumIdlingCycleAdjuster(var minimum: Double) : IdlingCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingIdlingCycleAdjuster(var step: Double) : IdlingCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingCycle, value: Double): Double = roundTo(value, step)
}

data class TieredIdlingCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentIdlingCycleAdjuster(var percent: Double) : IdlingCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the idling cycle steps in order and keeps a trace of each. */
data class IdlingCycleEngine(
    var steps: MutableList<IdlingCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingCycle): Double {
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

    fun evaluateAll(items: MutableList<IdlingCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingCycleEngine(): IdlingCycleEngine {
    return IdlingCycleEngine(
        steps = mutableListOf<IdlingCycleStep>(
            MinimumIdlingCycleAdjuster(15.0),
            RoundingIdlingCycleAdjuster(1.0),
            TieredIdlingCycleAdjuster(mutableListOf<Double>(2193.0, 7588.0), mutableListOf<Double>(1.09, 0.91)),
            PercentIdlingCycleAdjuster(5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 45290.0,
    )
}
