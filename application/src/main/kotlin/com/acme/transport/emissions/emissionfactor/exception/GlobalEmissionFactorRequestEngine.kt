package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global emission factor request calculation. */
interface GlobalEmissionFactorRequestModifier {
    fun describe(): String
    fun adjust(item: GlobalEmissionFactorRequest, value: Double): Double
}

data class RoundingGlobalEmissionFactorRequestAdjuster(var step: Double) : GlobalEmissionFactorRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalEmissionFactorRequest, value: Double): Double = roundTo(value, step)
}

data class CapGlobalEmissionFactorRequestAdjuster(var cap: Double) : GlobalEmissionFactorRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalEmissionFactorRequest, value: Double): Double = minOf(value, cap)
}

data class GlobalEmissionFactorRequestRevisionOffsetAdjuster(
    var weight: Double,
) : GlobalEmissionFactorRequestModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalEmissionFactorRequest, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class FlaggedGlobalEmissionFactorRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalEmissionFactorRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalEmissionFactorRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalEmissionFactorRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalEmissionFactorRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalEmissionFactorRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global emission factor request steps in order and keeps a trace of each. */
data class GlobalEmissionFactorRequestEngine(
    var steps: MutableList<GlobalEmissionFactorRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalEmissionFactorRequest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalEmissionFactorRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalEmissionFactorRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalEmissionFactorRequestEngine(flags: Registry): GlobalEmissionFactorRequestEngine {
    return GlobalEmissionFactorRequestEngine(
        steps = mutableListOf<GlobalEmissionFactorRequestModifier>(
            RoundingGlobalEmissionFactorRequestAdjuster(0.1),
            CapGlobalEmissionFactorRequestAdjuster(45256.0),
            GlobalEmissionFactorRequestRevisionOffsetAdjuster(0.36),
            FlaggedGlobalEmissionFactorRequestAdjuster(flags, "global-emission-factor-request-regional", -9.2),
            TieredGlobalEmissionFactorRequestAdjuster(
                mutableListOf<Double>(609.0, 2319.0, 6162.0),
                mutableListOf<Double>(0.92, 0.81, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 33257.8,
    )
}
