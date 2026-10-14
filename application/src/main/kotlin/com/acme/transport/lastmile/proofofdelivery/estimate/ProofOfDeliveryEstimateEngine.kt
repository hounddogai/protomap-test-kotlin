package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery estimate calculation. */
interface ProofOfDeliveryEstimateStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryEstimate, value: Double): Double
}

data class TieredProofOfDeliveryEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotProofOfDeliveryEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryEstimateStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ProofOfDeliveryEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumProofOfDeliveryEstimateAdjuster(var minimum: Double) : ProofOfDeliveryEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingProofOfDeliveryEstimateAdjuster(var step: Double) : ProofOfDeliveryEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the proof of delivery estimate steps in order and keeps a trace of each. */
data class ProofOfDeliveryEstimateEngine(
    var steps: MutableList<ProofOfDeliveryEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ProofOfDeliveryEstimate): Double {
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryEstimateEngine(): ProofOfDeliveryEstimateEngine {
    return ProofOfDeliveryEstimateEngine(
        steps = mutableListOf<ProofOfDeliveryEstimateStep>(
            TieredProofOfDeliveryEstimateAdjuster(
                mutableListOf<Double>(520.0, 6775.0, 6936.0, 9661.0),
                mutableListOf<Double>(0.95, 0.96, 0.92, 1.12),
            ),
            DepotProofOfDeliveryEstimateAdjuster(mutableMapOf<String, Double>("central" to 1.07)),
            MinimumProofOfDeliveryEstimateAdjuster(5.5),
            RoundingProofOfDeliveryEstimateAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 20335.4,
    )
}
