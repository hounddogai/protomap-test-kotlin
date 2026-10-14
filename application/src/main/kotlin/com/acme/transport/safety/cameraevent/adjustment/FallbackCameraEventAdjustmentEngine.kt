package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback camera event adjustment calculation. */
interface FallbackCameraEventAdjustmentStep {
    fun describe(): String
    fun adjust(item: FallbackCameraEventAdjustment, value: Double): Double
}

data class FallbackCameraEventAdjustmentDiscountOffsetAdjuster(
    var weight: Double,
) : FallbackCameraEventAdjustmentStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: FallbackCameraEventAdjustment, value: Double): Double = value + item.discount * weight
}

data class LabelFallbackCameraEventAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCameraEventAdjustmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackCameraEventAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackCameraEventAdjustmentAdjuster(
    var minimum: Double,
) : FallbackCameraEventAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCameraEventAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback camera event adjustment steps in order and keeps a trace of each. */
data class FallbackCameraEventAdjustmentEngine(
    var steps: MutableList<FallbackCameraEventAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackCameraEventAdjustment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCameraEventAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackCameraEventAdjustmentEngine(): FallbackCameraEventAdjustmentEngine {
    return FallbackCameraEventAdjustmentEngine(
        steps = mutableListOf<FallbackCameraEventAdjustmentStep>(
            FallbackCameraEventAdjustmentDiscountOffsetAdjuster(0.23),
            LabelFallbackCameraEventAdjustmentAdjuster(mutableMapOf<String, Double>("east" to 1.05)),
            MinimumFallbackCameraEventAdjustmentAdjuster(49.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 31385.6,
    )
}
