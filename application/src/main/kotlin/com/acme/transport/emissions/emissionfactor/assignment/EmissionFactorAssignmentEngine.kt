package com.acme.transport.emissions.emissionfactor.assignment

/** One step of the emission factor assignment calculation. */
interface EmissionFactorAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorAssignment, value: Double): Double
}

data class NameEmissionFactorAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorAssignmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: EmissionFactorAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumEmissionFactorAssignmentAdjuster(var minimum: Double) : EmissionFactorAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentEmissionFactorAssignmentAdjuster(var percent: Double) : EmissionFactorAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredEmissionFactorAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the emission factor assignment steps in order and keeps a trace of each. */
data class EmissionFactorAssignmentEngine(
    var steps: MutableList<EmissionFactorAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorAssignment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorAssignmentEngine(): EmissionFactorAssignmentEngine {
    return EmissionFactorAssignmentEngine(
        steps = mutableListOf<EmissionFactorAssignmentAdjuster>(
            NameEmissionFactorAssignmentAdjuster(mutableMapOf<String, Double>("north" to 1.0)),
            MinimumEmissionFactorAssignmentAdjuster(30.0),
            PercentEmissionFactorAssignmentAdjuster(-18.3),
            TieredEmissionFactorAssignmentAdjuster(
                mutableListOf<Double>(3957.0, 6497.0, 9387.0),
                mutableListOf<Double>(0.84, 1.05, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 91400.7,
    )
}
