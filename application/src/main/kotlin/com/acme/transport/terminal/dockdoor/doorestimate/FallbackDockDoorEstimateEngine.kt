package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback dock door estimate calculation. */
interface FallbackDockDoorEstimateAdjuster {
    fun describe(): String
    fun adjust(item: FallbackDockDoorEstimate, value: Double): Double
}

data class PercentFallbackDockDoorEstimateAdjuster(var percent: Double) : FallbackDockDoorEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDockDoorEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackDockDoorEstimateMinimumOffsetAdjuster(
    var weight: Double,
) : FallbackDockDoorEstimateAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: FallbackDockDoorEstimate, value: Double): Double = value + item.minimum * weight
}

data class CapFallbackDockDoorEstimateAdjuster(var cap: Double) : FallbackDockDoorEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDockDoorEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackDockDoorEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDockDoorEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDockDoorEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback dock door estimate steps in order and keeps a trace of each. */
data class FallbackDockDoorEstimateEngine(
    var steps: MutableList<FallbackDockDoorEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDockDoorEstimate): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDockDoorEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDockDoorEstimateEngine(): FallbackDockDoorEstimateEngine {
    return FallbackDockDoorEstimateEngine(
        steps = mutableListOf<FallbackDockDoorEstimateAdjuster>(
            PercentFallbackDockDoorEstimateAdjuster(22.6),
            FallbackDockDoorEstimateMinimumOffsetAdjuster(0.57),
            CapFallbackDockDoorEstimateAdjuster(24913.0),
            TieredFallbackDockDoorEstimateAdjuster(
                mutableListOf<Double>(6815.0, 7543.0),
                mutableListOf<Double>(1.1, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 90811.5,
    )
}
