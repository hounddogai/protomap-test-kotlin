package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cutoff rollup calculation. */
interface CutoffRollupModifier {
    fun describe(): String
    fun adjust(item: CutoffRollup, value: Double): Double
}

data class PercentCutoffRollupAdjuster(var percent: Double) : CutoffRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CutoffRollupPalletsOffsetAdjuster(var weight: Double) : CutoffRollupModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CutoffRollup, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class MinimumCutoffRollupAdjuster(var minimum: Double) : CutoffRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cutoff rollup steps in order and keeps a trace of each. */
data class CutoffRollupEngine(
    var steps: MutableList<CutoffRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffRollup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffRollupEngine(): CutoffRollupEngine {
    return CutoffRollupEngine(
        steps = mutableListOf<CutoffRollupModifier>(
            PercentCutoffRollupAdjuster(-15.6),
            CutoffRollupPalletsOffsetAdjuster(1.32),
            MinimumCutoffRollupAdjuster(33.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 34784.1,
    )
}
