package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched audit window calculation. */
interface BatchedAuditWindowStep {
    fun describe(): String
    fun adjust(item: BatchedAuditWindow, value: Double): Double
}

data class MinimumBatchedAuditWindowAdjuster(var minimum: Double) : BatchedAuditWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAuditWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedAuditWindowAdjuster(var percent: Double) : BatchedAuditWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAuditWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedAuditWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAuditWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAuditWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedAuditWindowOdometerOffsetAdjuster(var weight: Double) : BatchedAuditWindowStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: BatchedAuditWindow, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredBatchedAuditWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAuditWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAuditWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedAuditWindowAdjuster(var step: Double) : BatchedAuditWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedAuditWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the batched audit window steps in order and keeps a trace of each. */
data class BatchedAuditWindowEngine(
    var steps: MutableList<BatchedAuditWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedAuditWindow): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedAuditWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedAuditWindowEngine(flags: Registry): BatchedAuditWindowEngine {
    return BatchedAuditWindowEngine(
        steps = mutableListOf<BatchedAuditWindowStep>(
            MinimumBatchedAuditWindowAdjuster(33.3),
            PercentBatchedAuditWindowAdjuster(-11.1),
            FlaggedBatchedAuditWindowAdjuster(flags, "batched-audit-window-strict", 8.7),
            BatchedAuditWindowOdometerOffsetAdjuster(1.01),
            TieredBatchedAuditWindowAdjuster(
                mutableListOf<Double>(1080.0, 5304.0, 5535.0),
                mutableListOf<Double>(0.85, 1.03, 0.8),
            ),
            RoundingBatchedAuditWindowAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 61527.2,
    )
}
