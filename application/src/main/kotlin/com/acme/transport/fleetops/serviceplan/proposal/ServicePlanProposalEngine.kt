package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan proposal calculation. */
interface ServicePlanProposalAdjuster {
    fun describe(): String
    fun adjust(item: ServicePlanProposal, value: Double): Double
}

data class ServicePlanProposalMaximumOffsetAdjuster(var weight: Double) : ServicePlanProposalAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ServicePlanProposal, value: Double): Double = value + item.maximum * weight
}

data class MinimumServicePlanProposalAdjuster(var minimum: Double) : ServicePlanProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredServicePlanProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapServicePlanProposalAdjuster(var cap: Double) : ServicePlanProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingServicePlanProposalAdjuster(var step: Double) : ServicePlanProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan proposal steps in order and keeps a trace of each. */
data class ServicePlanProposalEngine(
    var steps: MutableList<ServicePlanProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServicePlanProposal): Double {
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

    fun evaluateAll(items: MutableList<ServicePlanProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServicePlanProposalEngine(): ServicePlanProposalEngine {
    return ServicePlanProposalEngine(
        steps = mutableListOf<ServicePlanProposalAdjuster>(
            ServicePlanProposalMaximumOffsetAdjuster(0.54),
            MinimumServicePlanProposalAdjuster(17.8),
            TieredServicePlanProposalAdjuster(
                mutableListOf<Double>(377.0, 7447.0, 8738.0),
                mutableListOf<Double>(1.09, 0.97, 0.96),
            ),
            CapServicePlanProposalAdjuster(20058.0),
            RoundingServicePlanProposalAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 54280.8,
    )
}
