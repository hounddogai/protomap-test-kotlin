package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback service area adjustment calculation. */
interface FallbackServiceAreaAdjustmentStep {
    fun describe(): String
    fun adjust(item: FallbackServiceAreaAdjustment, value: Double): Double
}

data class DepotFallbackServiceAreaAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackServiceAreaAdjustmentStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackServiceAreaAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackServiceAreaAdjustmentFloorValueOffsetAdjuster(
    var weight: Double,
) : FallbackServiceAreaAdjustmentStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: FallbackServiceAreaAdjustment, value: Double): Double = value + item.floorValue * weight
}

data class MinimumFallbackServiceAreaAdjustmentAdjuster(
    var minimum: Double,
) : FallbackServiceAreaAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackServiceAreaAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback service area adjustment steps in order and keeps a trace of each. */
data class FallbackServiceAreaAdjustmentEngine(
    var steps: MutableList<FallbackServiceAreaAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackServiceAreaAdjustment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackServiceAreaAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackServiceAreaAdjustmentEngine(): FallbackServiceAreaAdjustmentEngine {
    return FallbackServiceAreaAdjustmentEngine(
        steps = mutableListOf<FallbackServiceAreaAdjustmentStep>(
            DepotFallbackServiceAreaAdjustmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.01, "south" to 0.82),
            ),
            FallbackServiceAreaAdjustmentFloorValueOffsetAdjuster(0.17),
            MinimumFallbackServiceAreaAdjustmentAdjuster(20.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 30504.8,
    )
}
