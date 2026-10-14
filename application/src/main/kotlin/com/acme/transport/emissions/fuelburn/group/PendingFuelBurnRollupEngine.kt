package com.acme.transport.emissions.fuelburn.group

/** One step of the pending fuel burn rollup calculation. */
interface PendingFuelBurnRollupAdjuster {
    fun describe(): String
    fun adjust(item: PendingFuelBurnRollup, value: Double): Double
}

data class PendingFuelBurnRollupWeightOffsetAdjuster(var weight: Double) : PendingFuelBurnRollupAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: PendingFuelBurnRollup, value: Double): Double = value + item.weight * weight
}

data class CapPendingFuelBurnRollupAdjuster(var cap: Double) : PendingFuelBurnRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingFuelBurnRollup, value: Double): Double = minOf(value, cap)
}

data class PercentPendingFuelBurnRollupAdjuster(var percent: Double) : PendingFuelBurnRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFuelBurnRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingFuelBurnRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingFuelBurnRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingFuelBurnRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending fuel burn rollup steps in order and keeps a trace of each. */
data class PendingFuelBurnRollupEngine(
    var steps: MutableList<PendingFuelBurnRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingFuelBurnRollup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingFuelBurnRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingFuelBurnRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingFuelBurnRollupEngine(): PendingFuelBurnRollupEngine {
    return PendingFuelBurnRollupEngine(
        steps = mutableListOf<PendingFuelBurnRollupAdjuster>(
            PendingFuelBurnRollupWeightOffsetAdjuster(1.75),
            CapPendingFuelBurnRollupAdjuster(14575.0),
            PercentPendingFuelBurnRollupAdjuster(1.2),
            TieredPendingFuelBurnRollupAdjuster(
                mutableListOf<Double>(4322.0, 7483.0),
                mutableListOf<Double>(0.98, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 60878.6,
    )
}
