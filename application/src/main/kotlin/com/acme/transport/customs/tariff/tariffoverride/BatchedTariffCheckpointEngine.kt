package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched tariff checkpoint calculation. */
interface BatchedTariffCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: BatchedTariffCheckpoint, value: Double): Double
}

data class RoundingBatchedTariffCheckpointAdjuster(var step: Double) : BatchedTariffCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTariffCheckpoint, value: Double): Double = roundTo(value, step)
}

data class BatchedTariffCheckpointStartsAtOffsetAdjuster(var weight: Double) : BatchedTariffCheckpointAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: BatchedTariffCheckpoint, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class MinimumBatchedTariffCheckpointAdjuster(var minimum: Double) : BatchedTariffCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTariffCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedTariffCheckpointAdjuster(var cap: Double) : BatchedTariffCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTariffCheckpoint, value: Double): Double = minOf(value, cap)
}

data class LabelBatchedTariffCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTariffCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedTariffCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedTariffCheckpointAdjuster(var percent: Double) : BatchedTariffCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTariffCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched tariff checkpoint steps in order and keeps a trace of each. */
data class BatchedTariffCheckpointEngine(
    var steps: MutableList<BatchedTariffCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedTariffCheckpoint): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTariffCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedTariffCheckpointEngine(): BatchedTariffCheckpointEngine {
    return BatchedTariffCheckpointEngine(
        steps = mutableListOf<BatchedTariffCheckpointAdjuster>(
            RoundingBatchedTariffCheckpointAdjuster(5.0),
            BatchedTariffCheckpointStartsAtOffsetAdjuster(0.31),
            MinimumBatchedTariffCheckpointAdjuster(23.0),
            CapBatchedTariffCheckpointAdjuster(7658.0),
            LabelBatchedTariffCheckpointAdjuster(mutableMapOf<String, Double>("east" to 1.05)),
            PercentBatchedTariffCheckpointAdjuster(19.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 36106.8,
    )
}
