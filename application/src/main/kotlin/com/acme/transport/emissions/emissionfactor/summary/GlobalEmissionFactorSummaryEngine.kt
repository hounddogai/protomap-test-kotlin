package com.acme.transport.emissions.emissionfactor.summary

/** One step of the global emission factor summary calculation. */
interface GlobalEmissionFactorSummaryAdjuster {
    fun describe(): String
    fun adjust(item: GlobalEmissionFactorSummary, value: Double): Double
}

data class GlobalEmissionFactorSummaryRevisionOffsetAdjuster(
    var weight: Double,
) : GlobalEmissionFactorSummaryAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalEmissionFactorSummary, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class CapGlobalEmissionFactorSummaryAdjuster(var cap: Double) : GlobalEmissionFactorSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalEmissionFactorSummary, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalEmissionFactorSummaryAdjuster(
    var percent: Double,
) : GlobalEmissionFactorSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalEmissionFactorSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalEmissionFactorSummaryAdjuster(
    var minimum: Double,
) : GlobalEmissionFactorSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalEmissionFactorSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global emission factor summary steps in order and keeps a trace of each. */
data class GlobalEmissionFactorSummaryEngine(
    var steps: MutableList<GlobalEmissionFactorSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalEmissionFactorSummary): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalEmissionFactorSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalEmissionFactorSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalEmissionFactorSummaryEngine(): GlobalEmissionFactorSummaryEngine {
    return GlobalEmissionFactorSummaryEngine(
        steps = mutableListOf<GlobalEmissionFactorSummaryAdjuster>(
            GlobalEmissionFactorSummaryRevisionOffsetAdjuster(0.31),
            CapGlobalEmissionFactorSummaryAdjuster(6254.0),
            PercentGlobalEmissionFactorSummaryAdjuster(-14.0),
            MinimumGlobalEmissionFactorSummaryAdjuster(9.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 75030.5,
    )
}
