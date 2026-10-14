package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the idling override calculation. */
interface IdlingOverrideAdjuster {
    fun describe(): String
    fun adjust(item: IdlingOverride, value: Double): Double
}

data class IdlingOverrideDiscountOffsetAdjuster(var weight: Double) : IdlingOverrideAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: IdlingOverride, value: Double): Double = value + item.discount * weight
}

data class RoundingIdlingOverrideAdjuster(var step: Double) : IdlingOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingOverride, value: Double): Double = roundTo(value, step)
}

data class CapIdlingOverrideAdjuster(var cap: Double) : IdlingOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingOverride, value: Double): Double = minOf(value, cap)
}

data class PercentIdlingOverrideAdjuster(var percent: Double) : IdlingOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the idling override steps in order and keeps a trace of each. */
data class IdlingOverrideEngine(
    var steps: MutableList<IdlingOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingOverride): Double {
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

    fun evaluateAll(items: MutableList<IdlingOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingOverrideEngine(): IdlingOverrideEngine {
    return IdlingOverrideEngine(
        steps = mutableListOf<IdlingOverrideAdjuster>(
            IdlingOverrideDiscountOffsetAdjuster(1.66),
            RoundingIdlingOverrideAdjuster(0.5),
            CapIdlingOverrideAdjuster(15324.0),
            PercentIdlingOverrideAdjuster(-20.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 68020.4,
    )
}
