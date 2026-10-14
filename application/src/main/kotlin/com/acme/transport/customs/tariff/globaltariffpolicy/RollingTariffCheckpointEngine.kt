package com.acme.transport.customs.tariff.globaltariffpolicy

/** One step of the rolling tariff checkpoint calculation. */
interface RollingTariffCheckpointModifier {
    fun describe(): String
    fun adjust(item: RollingTariffCheckpoint, value: Double): Double
}

data class TieredRollingTariffCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTariffCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTariffCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingTariffCheckpointAdjuster(var cap: Double) : RollingTariffCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTariffCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingTariffCheckpointAdjuster(var minimum: Double) : RollingTariffCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTariffCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling tariff checkpoint steps in order and keeps a trace of each. */
data class RollingTariffCheckpointEngine(
    var steps: MutableList<RollingTariffCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingTariffCheckpoint): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTariffCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTariffCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingTariffCheckpointEngine(): RollingTariffCheckpointEngine {
    return RollingTariffCheckpointEngine(
        steps = mutableListOf<RollingTariffCheckpointModifier>(
            TieredRollingTariffCheckpointAdjuster(
                mutableListOf<Double>(4448.0, 9005.0),
                mutableListOf<Double>(1.12, 1.03),
            ),
            CapRollingTariffCheckpointAdjuster(26928.0),
            MinimumRollingTariffCheckpointAdjuster(3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 89074.5,
    )
}
