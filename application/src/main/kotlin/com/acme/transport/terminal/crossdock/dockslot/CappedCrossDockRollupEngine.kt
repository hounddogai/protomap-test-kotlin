package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped cross dock rollup calculation. */
interface CappedCrossDockRollupAdjuster {
    fun describe(): String
    fun adjust(item: CappedCrossDockRollup, value: Double): Double
}

data class CapCappedCrossDockRollupAdjuster(var cap: Double) : CappedCrossDockRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCrossDockRollup, value: Double): Double = minOf(value, cap)
}

data class NameCappedCrossDockRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCrossDockRollupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedCrossDockRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedCrossDockRollupAdjuster(var percent: Double) : CappedCrossDockRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCrossDockRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedCrossDockRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCrossDockRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCrossDockRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped cross dock rollup steps in order and keeps a trace of each. */
data class CappedCrossDockRollupEngine(
    var steps: MutableList<CappedCrossDockRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCrossDockRollup): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedCrossDockRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCrossDockRollupEngine(): CappedCrossDockRollupEngine {
    return CappedCrossDockRollupEngine(
        steps = mutableListOf<CappedCrossDockRollupAdjuster>(
            CapCappedCrossDockRollupAdjuster(23358.0),
            NameCappedCrossDockRollupAdjuster(mutableMapOf<String, Double>("retail" to 1.1, "online" to 0.78)),
            PercentCappedCrossDockRollupAdjuster(0.8),
            TieredCappedCrossDockRollupAdjuster(
                mutableListOf<Double>(5406.0, 6016.0, 8233.0),
                mutableListOf<Double>(0.98, 0.85, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 45590.3,
    )
}
