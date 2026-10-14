package com.acme.transport.emissions.emissionfactor.cycle

/** One step of the emission factor checkpoint calculation. */
interface EmissionFactorCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorCheckpoint, value: Double): Double
}

data class PercentEmissionFactorCheckpointAdjuster(var percent: Double) : EmissionFactorCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredEmissionFactorCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class EmissionFactorCheckpointQuantityOffsetAdjuster(
    var weight: Double,
) : EmissionFactorCheckpointAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: EmissionFactorCheckpoint, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class MinimumEmissionFactorCheckpointAdjuster(var minimum: Double) : EmissionFactorCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the emission factor checkpoint steps in order and keeps a trace of each. */
data class EmissionFactorCheckpointEngine(
    var steps: MutableList<EmissionFactorCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorCheckpointEngine(): EmissionFactorCheckpointEngine {
    return EmissionFactorCheckpointEngine(
        steps = mutableListOf<EmissionFactorCheckpointAdjuster>(
            PercentEmissionFactorCheckpointAdjuster(25.0),
            TieredEmissionFactorCheckpointAdjuster(
                mutableListOf<Double>(1275.0, 2067.0),
                mutableListOf<Double>(1.06, 1.09),
            ),
            EmissionFactorCheckpointQuantityOffsetAdjuster(1.49),
            MinimumEmissionFactorCheckpointAdjuster(10.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 81010.5,
    )
}
