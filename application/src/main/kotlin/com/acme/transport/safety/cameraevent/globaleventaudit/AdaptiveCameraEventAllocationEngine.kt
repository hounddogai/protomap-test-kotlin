package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive camera event allocation calculation. */
interface AdaptiveCameraEventAllocationModifier {
    fun describe(): String
    fun adjust(item: AdaptiveCameraEventAllocation, value: Double): Double
}

data class MinimumAdaptiveCameraEventAllocationAdjuster(
    var minimum: Double,
) : AdaptiveCameraEventAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCameraEventAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveCameraEventAllocationVersionOffsetAdjuster(
    var weight: Double,
) : AdaptiveCameraEventAllocationModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: AdaptiveCameraEventAllocation, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingAdaptiveCameraEventAllocationAdjuster(
    var step: Double,
) : AdaptiveCameraEventAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCameraEventAllocation, value: Double): Double = roundTo(value, step)
}

data class LabelAdaptiveCameraEventAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCameraEventAllocationModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveCameraEventAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive camera event allocation steps in order and keeps a trace of each. */
data class AdaptiveCameraEventAllocationEngine(
    var steps: MutableList<AdaptiveCameraEventAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveCameraEventAllocation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveCameraEventAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveCameraEventAllocationEngine(): AdaptiveCameraEventAllocationEngine {
    return AdaptiveCameraEventAllocationEngine(
        steps = mutableListOf<AdaptiveCameraEventAllocationModifier>(
            MinimumAdaptiveCameraEventAllocationAdjuster(14.4),
            AdaptiveCameraEventAllocationVersionOffsetAdjuster(0.05),
            RoundingAdaptiveCameraEventAllocationAdjuster(1.0),
            LabelAdaptiveCameraEventAllocationAdjuster(
                mutableMapOf<String, Double>("online" to 1.18, "east" to 1.04, "retail" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 26258.8,
    )
}
