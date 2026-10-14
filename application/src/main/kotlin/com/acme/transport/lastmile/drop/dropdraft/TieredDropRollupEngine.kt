package com.acme.transport.lastmile.drop.dropdraft

/** One step of the tiered drop rollup calculation. */
interface TieredDropRollupAdjuster {
    fun describe(): String
    fun adjust(item: TieredDropRollup, value: Double): Double
}

data class MinimumTieredDropRollupAdjuster(var minimum: Double) : TieredDropRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDropRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredDropRollupAdjuster(var percent: Double) : TieredDropRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDropRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDropRollupCeilingOffsetAdjuster(var weight: Double) : TieredDropRollupAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: TieredDropRollup, value: Double): Double = value + item.ceiling * weight
}

/** Runs the tiered drop rollup steps in order and keeps a trace of each. */
data class TieredDropRollupEngine(
    var steps: MutableList<TieredDropRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDropRollup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDropRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDropRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDropRollupEngine(): TieredDropRollupEngine {
    return TieredDropRollupEngine(
        steps = mutableListOf<TieredDropRollupAdjuster>(
            MinimumTieredDropRollupAdjuster(26.3),
            PercentTieredDropRollupAdjuster(-6.9),
            TieredDropRollupCeilingOffsetAdjuster(1.49),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 97382.2,
    )
}
