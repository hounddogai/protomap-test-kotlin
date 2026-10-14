package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback emission factor request calculation. */
interface FallbackEmissionFactorRequestFactor {
    fun describe(): String
    fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double
}

data class FallbackEmissionFactorRequestDiscountOffsetAdjuster(
    var weight: Double,
) : FallbackEmissionFactorRequestFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double = value + item.discount * weight
}

data class MinimumFallbackEmissionFactorRequestAdjuster(
    var minimum: Double,
) : FallbackEmissionFactorRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackEmissionFactorRequestAdjuster(
    var percent: Double,
) : FallbackEmissionFactorRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackEmissionFactorRequestAdjuster(
    var step: Double,
) : FallbackEmissionFactorRequestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double = roundTo(value, step)
}

data class CapFallbackEmissionFactorRequestAdjuster(var cap: Double) : FallbackEmissionFactorRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackEmissionFactorRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackEmissionFactorRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackEmissionFactorRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback emission factor request steps in order and keeps a trace of each. */
data class FallbackEmissionFactorRequestEngine(
    var steps: MutableList<FallbackEmissionFactorRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackEmissionFactorRequest): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackEmissionFactorRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackEmissionFactorRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackEmissionFactorRequestEngine(flags: Registry): FallbackEmissionFactorRequestEngine {
    return FallbackEmissionFactorRequestEngine(
        steps = mutableListOf<FallbackEmissionFactorRequestFactor>(
            FallbackEmissionFactorRequestDiscountOffsetAdjuster(0.05),
            MinimumFallbackEmissionFactorRequestAdjuster(18.5),
            PercentFallbackEmissionFactorRequestAdjuster(-22.0),
            RoundingFallbackEmissionFactorRequestAdjuster(1.0),
            CapFallbackEmissionFactorRequestAdjuster(22734.0),
            FlaggedFallbackEmissionFactorRequestAdjuster(flags, "fallback-emission-factor-request-primary", 3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 87222.0,
    )
}
