package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity batch calculation. */
interface CapacityBatchFactor {
    fun describe(): String
    fun adjust(item: CapacityBatch, value: Double): Double
}

data class TitleCapacityBatchAdjuster(var factors: MutableMap<String, Double>) : CapacityBatchFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: CapacityBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCapacityBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapacityBatchThresholdOffsetAdjuster(var weight: Double) : CapacityBatchFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CapacityBatch, value: Double): Double = value + item.threshold * weight
}

data class CapCapacityBatchAdjuster(var cap: Double) : CapacityBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the capacity batch steps in order and keeps a trace of each. */
data class CapacityBatchEngine(
    var steps: MutableList<CapacityBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityBatch): Double {
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

    fun evaluateAll(items: MutableList<CapacityBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityBatchEngine(flags: Registry): CapacityBatchEngine {
    return CapacityBatchEngine(
        steps = mutableListOf<CapacityBatchFactor>(
            TitleCapacityBatchAdjuster(mutableMapOf<String, Double>("east" to 1.25, "west" to 0.95)),
            FlaggedCapacityBatchAdjuster(flags, "capacity-batch-lenient", 4.9),
            CapacityBatchThresholdOffsetAdjuster(0.43),
            CapCapacityBatchAdjuster(35183.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 39109.6,
    )
}
