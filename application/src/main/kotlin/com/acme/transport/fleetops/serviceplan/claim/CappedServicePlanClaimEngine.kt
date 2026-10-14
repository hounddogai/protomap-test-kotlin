package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped service plan claim calculation. */
interface CappedServicePlanClaimAdjuster {
    fun describe(): String
    fun adjust(item: CappedServicePlanClaim, value: Double): Double
}

data class PercentCappedServicePlanClaimAdjuster(var percent: Double) : CappedServicePlanClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedServicePlanClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedServicePlanClaimAdjuster(var cap: Double) : CappedServicePlanClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedServicePlanClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedServicePlanClaimAdjuster(var minimum: Double) : CappedServicePlanClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedServicePlanClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedServicePlanClaimAdjuster(var step: Double) : CappedServicePlanClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedServicePlanClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the capped service plan claim steps in order and keeps a trace of each. */
data class CappedServicePlanClaimEngine(
    var steps: MutableList<CappedServicePlanClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServicePlanClaim): Double {
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

    fun evaluateAll(items: MutableList<CappedServicePlanClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServicePlanClaimEngine(): CappedServicePlanClaimEngine {
    return CappedServicePlanClaimEngine(
        steps = mutableListOf<CappedServicePlanClaimAdjuster>(
            PercentCappedServicePlanClaimAdjuster(19.3),
            CapCappedServicePlanClaimAdjuster(30260.0),
            MinimumCappedServicePlanClaimAdjuster(44.7),
            RoundingCappedServicePlanClaimAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 66755.9,
    )
}
