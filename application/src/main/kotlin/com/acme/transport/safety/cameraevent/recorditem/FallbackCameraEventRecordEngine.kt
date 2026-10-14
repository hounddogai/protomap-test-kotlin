package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback camera event record calculation. */
interface FallbackCameraEventRecordFactor {
    fun describe(): String
    fun adjust(item: FallbackCameraEventRecord, value: Double): Double
}

data class SourceFallbackCameraEventRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCameraEventRecordFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: FallbackCameraEventRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackCameraEventRecordAdjuster(var cap: Double) : FallbackCameraEventRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCameraEventRecord, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackCameraEventRecordAdjuster(var percent: Double) : FallbackCameraEventRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCameraEventRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackCameraEventRecordAdjuster(var minimum: Double) : FallbackCameraEventRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCameraEventRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackCameraEventRecordAdjuster(var step: Double) : FallbackCameraEventRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCameraEventRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback camera event record steps in order and keeps a trace of each. */
data class FallbackCameraEventRecordEngine(
    var steps: MutableList<FallbackCameraEventRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackCameraEventRecord): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCameraEventRecordFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCameraEventRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackCameraEventRecordEngine(): FallbackCameraEventRecordEngine {
    return FallbackCameraEventRecordEngine(
        steps = mutableListOf<FallbackCameraEventRecordFactor>(
            SourceFallbackCameraEventRecordAdjuster(
                mutableMapOf<String, Double>("west" to 0.8, "north" to 1.15, "retail" to 1.3),
            ),
            CapFallbackCameraEventRecordAdjuster(40471.0),
            PercentFallbackCameraEventRecordAdjuster(-8.4),
            MinimumFallbackCameraEventRecordAdjuster(40.1),
            RoundingFallbackCameraEventRecordAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 75273.2,
    )
}
