package com.acme.transport.emissions.evcharge.overrideitem

/** One step of the tiered ev charge checkpoint calculation. */
interface TieredEvChargeCheckpointStep {
    fun describe(): String
    fun adjust(item: TieredEvChargeCheckpoint, value: Double): Double
}

data class PercentTieredEvChargeCheckpointAdjuster(var percent: Double) : TieredEvChargeCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredEvChargeCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredEvChargeCheckpointScoreOffsetAdjuster(var weight: Double) : TieredEvChargeCheckpointStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TieredEvChargeCheckpoint, value: Double): Double = value + item.score * weight
}

data class TieredTieredEvChargeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredEvChargeCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredEvChargeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered ev charge checkpoint steps in order and keeps a trace of each. */
data class TieredEvChargeCheckpointEngine(
    var steps: MutableList<TieredEvChargeCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredEvChargeCheckpoint): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredEvChargeCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredEvChargeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredEvChargeCheckpointEngine(): TieredEvChargeCheckpointEngine {
    return TieredEvChargeCheckpointEngine(
        steps = mutableListOf<TieredEvChargeCheckpointStep>(
            PercentTieredEvChargeCheckpointAdjuster(-12.1),
            TieredEvChargeCheckpointScoreOffsetAdjuster(0.52),
            TieredTieredEvChargeCheckpointAdjuster(
                mutableListOf<Double>(439.0, 509.0, 5036.0, 5800.0),
                mutableListOf<Double>(1.08, 1.07, 1.15, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 93495.3,
    )
}
