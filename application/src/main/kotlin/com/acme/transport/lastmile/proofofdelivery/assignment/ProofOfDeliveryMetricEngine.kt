package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the proof of delivery metric calculation. */
interface ProofOfDeliveryMetricStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryMetric, value: Double): Double
}

data class NameProofOfDeliveryMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryMetricStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ProofOfDeliveryMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingProofOfDeliveryMetricAdjuster(var step: Double) : ProofOfDeliveryMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryMetric, value: Double): Double = roundTo(value, step)
}

data class ProofOfDeliveryMetricGraceDaysOffsetAdjuster(var weight: Double) : ProofOfDeliveryMetricStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ProofOfDeliveryMetric, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class CapProofOfDeliveryMetricAdjuster(var cap: Double) : ProofOfDeliveryMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryMetric, value: Double): Double = minOf(value, cap)
}

data class MinimumProofOfDeliveryMetricAdjuster(var minimum: Double) : ProofOfDeliveryMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredProofOfDeliveryMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the proof of delivery metric steps in order and keeps a trace of each. */
data class ProofOfDeliveryMetricEngine(
    var steps: MutableList<ProofOfDeliveryMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ProofOfDeliveryMetric): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryMetricEngine(): ProofOfDeliveryMetricEngine {
    return ProofOfDeliveryMetricEngine(
        steps = mutableListOf<ProofOfDeliveryMetricStep>(
            NameProofOfDeliveryMetricAdjuster(mutableMapOf<String, Double>("south" to 1.32)),
            RoundingProofOfDeliveryMetricAdjuster(0.5),
            ProofOfDeliveryMetricGraceDaysOffsetAdjuster(1.14),
            CapProofOfDeliveryMetricAdjuster(14679.0),
            MinimumProofOfDeliveryMetricAdjuster(11.2),
            TieredProofOfDeliveryMetricAdjuster(
                mutableListOf<Double>(3868.0, 5772.0, 7670.0),
                mutableListOf<Double>(0.97, 0.97, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 46167.9,
    )
}
