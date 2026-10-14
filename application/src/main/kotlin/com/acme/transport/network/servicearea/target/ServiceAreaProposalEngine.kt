package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area proposal calculation. */
interface ServiceAreaProposalAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaProposal, value: Double): Double
}

data class FlaggedServiceAreaProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingServiceAreaProposalAdjuster(var step: Double) : ServiceAreaProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaProposal, value: Double): Double = roundTo(value, step)
}

data class MinimumServiceAreaProposalAdjuster(var minimum: Double) : ServiceAreaProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredServiceAreaProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ServiceAreaProposalBatchSizeOffsetAdjuster(var weight: Double) : ServiceAreaProposalAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ServiceAreaProposal, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the service area proposal steps in order and keeps a trace of each. */
data class ServiceAreaProposalEngine(
    var steps: MutableList<ServiceAreaProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaProposal): Double {
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

    fun evaluateAll(items: MutableList<ServiceAreaProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaProposalEngine(flags: Registry): ServiceAreaProposalEngine {
    return ServiceAreaProposalEngine(
        steps = mutableListOf<ServiceAreaProposalAdjuster>(
            FlaggedServiceAreaProposalAdjuster(flags, "service-area-proposal-fallback", 3.0),
            RoundingServiceAreaProposalAdjuster(1.0),
            MinimumServiceAreaProposalAdjuster(18.8),
            TieredServiceAreaProposalAdjuster(
                mutableListOf<Double>(254.0, 8603.0, 8933.0, 9462.0),
                mutableListOf<Double>(1.13, 1.18, 1.14, 1.13),
            ),
            ServiceAreaProposalBatchSizeOffsetAdjuster(1.16),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 75168.9,
    )
}
