package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary clearance variance calculation. */
interface PrimaryClearanceVarianceStep {
    fun describe(): String
    fun adjust(item: PrimaryClearanceVariance, value: Double): Double
}

data class RoundingPrimaryClearanceVarianceAdjuster(var step: Double) : PrimaryClearanceVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryClearanceVariance, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryClearanceVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryClearanceVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryClearanceVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryClearanceVarianceExpiresAtOffsetAdjuster(var weight: Double) : PrimaryClearanceVarianceStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: PrimaryClearanceVariance, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the primary clearance variance steps in order and keeps a trace of each. */
data class PrimaryClearanceVarianceEngine(
    var steps: MutableList<PrimaryClearanceVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryClearanceVariance): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryClearanceVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryClearanceVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryClearanceVarianceEngine(): PrimaryClearanceVarianceEngine {
    return PrimaryClearanceVarianceEngine(
        steps = mutableListOf<PrimaryClearanceVarianceStep>(
            RoundingPrimaryClearanceVarianceAdjuster(0.5),
            TieredPrimaryClearanceVarianceAdjuster(
                mutableListOf<Double>(3508.0, 4193.0, 9422.0),
                mutableListOf<Double>(1.03, 0.88, 0.85),
            ),
            PrimaryClearanceVarianceExpiresAtOffsetAdjuster(1.84),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 70581.5,
    )
}
