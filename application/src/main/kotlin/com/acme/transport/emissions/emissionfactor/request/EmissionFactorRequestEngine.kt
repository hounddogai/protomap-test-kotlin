package com.acme.transport.emissions.emissionfactor.request

/** One step of the emission factor request calculation. */
interface EmissionFactorRequestFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorRequest, value: Double): Double
}

data class MinimumEmissionFactorRequestAdjuster(var minimum: Double) : EmissionFactorRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEmissionFactorRequestAdjuster(var cap: Double) : EmissionFactorRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorRequest, value: Double): Double = minOf(value, cap)
}

data class SourceEmissionFactorRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorRequestFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: EmissionFactorRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor request steps in order and keeps a trace of each. */
data class EmissionFactorRequestEngine(
    var steps: MutableList<EmissionFactorRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorRequest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorRequestEngine(): EmissionFactorRequestEngine {
    return EmissionFactorRequestEngine(
        steps = mutableListOf<EmissionFactorRequestFactor>(
            MinimumEmissionFactorRequestAdjuster(46.9),
            CapEmissionFactorRequestAdjuster(5912.0),
            SourceEmissionFactorRequestAdjuster(mutableMapOf<String, Double>("central" to 1.37)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 19155.2,
    )
}
