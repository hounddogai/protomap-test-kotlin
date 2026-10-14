package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict service area cycle calculation. */
interface StrictServiceAreaCycleStep {
    fun describe(): String
    fun adjust(item: StrictServiceAreaCycle, value: Double): Double
}

data class StrictServiceAreaCycleRatioOffsetAdjuster(var weight: Double) : StrictServiceAreaCycleStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: StrictServiceAreaCycle, value: Double): Double = value + item.ratio * weight
}

data class TieredStrictServiceAreaCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictServiceAreaCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictServiceAreaCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionStrictServiceAreaCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictServiceAreaCycleStep {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictServiceAreaCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict service area cycle steps in order and keeps a trace of each. */
data class StrictServiceAreaCycleEngine(
    var steps: MutableList<StrictServiceAreaCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictServiceAreaCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictServiceAreaCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictServiceAreaCycleEngine(): StrictServiceAreaCycleEngine {
    return StrictServiceAreaCycleEngine(
        steps = mutableListOf<StrictServiceAreaCycleStep>(
            StrictServiceAreaCycleRatioOffsetAdjuster(1.09),
            TieredStrictServiceAreaCycleAdjuster(
                mutableListOf<Double>(1500.0, 4133.0),
                mutableListOf<Double>(0.98, 0.88),
            ),
            RegionStrictServiceAreaCycleAdjuster(mutableMapOf<String, Double>("north" to 1.4, "retail" to 1.39)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 33839.2,
    )
}
