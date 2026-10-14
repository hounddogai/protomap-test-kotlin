package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict service plan claim calculation. */
interface StrictServicePlanClaimModifier {
    fun describe(): String
    fun adjust(item: StrictServicePlanClaim, value: Double): Double
}

data class MinimumStrictServicePlanClaimAdjuster(var minimum: Double) : StrictServicePlanClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictServicePlanClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictServicePlanClaimAdjuster(var step: Double) : StrictServicePlanClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictServicePlanClaim, value: Double): Double = roundTo(value, step)
}

data class PercentStrictServicePlanClaimAdjuster(var percent: Double) : StrictServicePlanClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictServicePlanClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict service plan claim steps in order and keeps a trace of each. */
data class StrictServicePlanClaimEngine(
    var steps: MutableList<StrictServicePlanClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictServicePlanClaim): Double {
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

    fun evaluateAll(items: MutableList<StrictServicePlanClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictServicePlanClaimEngine(): StrictServicePlanClaimEngine {
    return StrictServicePlanClaimEngine(
        steps = mutableListOf<StrictServicePlanClaimModifier>(
            MinimumStrictServicePlanClaimAdjuster(24.1),
            RoundingStrictServicePlanClaimAdjuster(0.1),
            PercentStrictServicePlanClaimAdjuster(8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 73081.1,
    )
}
