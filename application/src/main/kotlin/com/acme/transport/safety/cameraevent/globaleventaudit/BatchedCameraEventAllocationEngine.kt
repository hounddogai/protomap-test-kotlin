package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched camera event allocation calculation. */
interface BatchedCameraEventAllocationFactor {
    fun describe(): String
    fun adjust(item: BatchedCameraEventAllocation, value: Double): Double
}

data class FlaggedBatchedCameraEventAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCameraEventAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCameraEventAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedCameraEventAllocationRetriesOffsetAdjuster(
    var weight: Double,
) : BatchedCameraEventAllocationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: BatchedCameraEventAllocation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class RoundingBatchedCameraEventAllocationAdjuster(var step: Double) : BatchedCameraEventAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCameraEventAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedCameraEventAllocationAdjuster(
    var minimum: Double,
) : BatchedCameraEventAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCameraEventAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedCameraEventAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCameraEventAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCameraEventAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedCameraEventAllocationAdjuster(var cap: Double) : BatchedCameraEventAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCameraEventAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the batched camera event allocation steps in order and keeps a trace of each. */
data class BatchedCameraEventAllocationEngine(
    var steps: MutableList<BatchedCameraEventAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCameraEventAllocation): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCameraEventAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCameraEventAllocationEngine(flags: Registry): BatchedCameraEventAllocationEngine {
    return BatchedCameraEventAllocationEngine(
        steps = mutableListOf<BatchedCameraEventAllocationFactor>(
            FlaggedBatchedCameraEventAllocationAdjuster(flags, "batched-camera-event-allocation-capped", -8.4),
            BatchedCameraEventAllocationRetriesOffsetAdjuster(1.74),
            RoundingBatchedCameraEventAllocationAdjuster(0.5),
            MinimumBatchedCameraEventAllocationAdjuster(37.4),
            TieredBatchedCameraEventAllocationAdjuster(
                mutableListOf<Double>(2462.0, 4106.0, 5918.0, 7236.0),
                mutableListOf<Double>(0.99, 0.95, 0.99, 0.94),
            ),
            CapBatchedCameraEventAllocationAdjuster(17468.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 44907.0,
    )
}
