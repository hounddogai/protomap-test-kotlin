package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict declaration target calculation. */
interface StrictDeclarationTargetFactor {
    fun describe(): String
    fun adjust(item: StrictDeclarationTarget, value: Double): Double
}

data class PercentStrictDeclarationTargetAdjuster(var percent: Double) : StrictDeclarationTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDeclarationTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictDeclarationTargetWeightOffsetAdjuster(var weight: Double) : StrictDeclarationTargetFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: StrictDeclarationTarget, value: Double): Double = value + item.weight * weight
}

data class MinimumStrictDeclarationTargetAdjuster(var minimum: Double) : StrictDeclarationTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDeclarationTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict declaration target steps in order and keeps a trace of each. */
data class StrictDeclarationTargetEngine(
    var steps: MutableList<StrictDeclarationTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictDeclarationTarget): Double {
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

    fun evaluateAll(items: MutableList<StrictDeclarationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictDeclarationTargetEngine(): StrictDeclarationTargetEngine {
    return StrictDeclarationTargetEngine(
        steps = mutableListOf<StrictDeclarationTargetFactor>(
            PercentStrictDeclarationTargetAdjuster(-8.6),
            StrictDeclarationTargetWeightOffsetAdjuster(1.7),
            MinimumStrictDeclarationTargetAdjuster(48.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 73139.4,
    )
}
