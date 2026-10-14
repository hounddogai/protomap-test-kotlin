package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event batch calculation. */
interface CameraEventBatchStep {
    fun describe(): String
    fun adjust(item: CameraEventBatch, value: Double): Double
}

data class ChannelCameraEventBatchAdjuster(var factors: MutableMap<String, Double>) : CameraEventBatchStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: CameraEventBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCameraEventBatchAdjuster(var step: Double) : CameraEventBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventBatch, value: Double): Double = roundTo(value, step)
}

data class CapCameraEventBatchAdjuster(var cap: Double) : CameraEventBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventBatch, value: Double): Double = minOf(value, cap)
}

data class PercentCameraEventBatchAdjuster(var percent: Double) : CameraEventBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCameraEventBatchAdjuster(var minimum: Double) : CameraEventBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CameraEventBatchUpdatedAtOffsetAdjuster(var weight: Double) : CameraEventBatchStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: CameraEventBatch, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

/** Runs the camera event batch steps in order and keeps a trace of each. */
data class CameraEventBatchEngine(
    var steps: MutableList<CameraEventBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CameraEventBatch): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCameraEventBatchEngine(): CameraEventBatchEngine {
    return CameraEventBatchEngine(
        steps = mutableListOf<CameraEventBatchStep>(
            ChannelCameraEventBatchAdjuster(
                mutableMapOf<String, Double>("east" to 0.85, "south" to 1.31, "retail" to 1.14),
            ),
            RoundingCameraEventBatchAdjuster(0.05),
            CapCameraEventBatchAdjuster(30392.0),
            PercentCameraEventBatchAdjuster(2.3),
            MinimumCameraEventBatchAdjuster(36.5),
            CameraEventBatchUpdatedAtOffsetAdjuster(0.72),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 54377.2,
    )
}
