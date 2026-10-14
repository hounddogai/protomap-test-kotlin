package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive emission factor assignment calculation. */
interface AdaptiveEmissionFactorAssignmentStep {
    fun describe(): String
    fun adjust(item: AdaptiveEmissionFactorAssignment, value: Double): Double
}

data class FlaggedAdaptiveEmissionFactorAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveEmissionFactorAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveEmissionFactorAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveEmissionFactorAssignmentAdjuster(var cap: Double) : AdaptiveEmissionFactorAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveEmissionFactorAssignment, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveEmissionFactorAssignmentAdjuster(
    var percent: Double,
) : AdaptiveEmissionFactorAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveEmissionFactorAssignment, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive emission factor assignment steps in order and keeps a trace of each. */
data class AdaptiveEmissionFactorAssignmentEngine(
    var steps: MutableList<AdaptiveEmissionFactorAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveEmissionFactorAssignment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEmissionFactorAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEmissionFactorAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveEmissionFactorAssignmentEngine(flags: Registry): AdaptiveEmissionFactorAssignmentEngine {
    return AdaptiveEmissionFactorAssignmentEngine(
        steps = mutableListOf<AdaptiveEmissionFactorAssignmentStep>(
            FlaggedAdaptiveEmissionFactorAssignmentAdjuster(
                flags,
                "adaptive-emission-factor-assignment-rolling",
                -8.4,
            ),
            CapAdaptiveEmissionFactorAssignmentAdjuster(47163.0),
            PercentAdaptiveEmissionFactorAssignmentAdjuster(-22.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 60536.8,
    )
}
