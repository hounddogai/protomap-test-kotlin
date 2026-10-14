package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict zone cycle calculation. */
interface StrictZoneCycleAdjuster {
    fun describe(): String
    fun adjust(item: StrictZoneCycle, value: Double): Double
}

data class PercentStrictZoneCycleAdjuster(var percent: Double) : StrictZoneCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictZoneCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictZoneCycleCeilingOffsetAdjuster(var weight: Double) : StrictZoneCycleAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: StrictZoneCycle, value: Double): Double = value + item.ceiling * weight
}

data class TieredStrictZoneCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictZoneCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictZoneCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict zone cycle steps in order and keeps a trace of each. */
data class StrictZoneCycleEngine(
    var steps: MutableList<StrictZoneCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictZoneCycle): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictZoneCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictZoneCycleEngine(): StrictZoneCycleEngine {
    return StrictZoneCycleEngine(
        steps = mutableListOf<StrictZoneCycleAdjuster>(
            PercentStrictZoneCycleAdjuster(7.5),
            StrictZoneCycleCeilingOffsetAdjuster(1.54),
            TieredStrictZoneCycleAdjuster(
                mutableListOf<Double>(4087.0, 6785.0),
                mutableListOf<Double>(1.16, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 11325.2,
    )
}
