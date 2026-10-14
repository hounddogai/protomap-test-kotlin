package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary emission factor review calculation. */
interface PrimaryEmissionFactorReviewStep {
    fun describe(): String
    fun adjust(item: PrimaryEmissionFactorReview, value: Double): Double
}

data class PercentPrimaryEmissionFactorReviewAdjuster(var percent: Double) : PrimaryEmissionFactorReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryEmissionFactorReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryEmissionFactorReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryEmissionFactorReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryEmissionFactorReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPrimaryEmissionFactorReviewAdjuster(var cap: Double) : PrimaryEmissionFactorReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryEmissionFactorReview, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryEmissionFactorReviewAdjuster(var step: Double) : PrimaryEmissionFactorReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEmissionFactorReview, value: Double): Double = roundTo(value, step)
}

/** Runs the primary emission factor review steps in order and keeps a trace of each. */
data class PrimaryEmissionFactorReviewEngine(
    var steps: MutableList<PrimaryEmissionFactorReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryEmissionFactorReview): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryEmissionFactorReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryEmissionFactorReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryEmissionFactorReviewEngine(): PrimaryEmissionFactorReviewEngine {
    return PrimaryEmissionFactorReviewEngine(
        steps = mutableListOf<PrimaryEmissionFactorReviewStep>(
            PercentPrimaryEmissionFactorReviewAdjuster(-9.5),
            TieredPrimaryEmissionFactorReviewAdjuster(
                mutableListOf<Double>(6393.0, 8119.0),
                mutableListOf<Double>(0.86, 0.92),
            ),
            CapPrimaryEmissionFactorReviewAdjuster(20040.0),
            RoundingPrimaryEmissionFactorReviewAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 18603.0,
    )
}
