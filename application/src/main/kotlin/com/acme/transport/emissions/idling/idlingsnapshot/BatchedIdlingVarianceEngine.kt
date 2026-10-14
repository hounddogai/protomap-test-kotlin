package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched idling variance calculation. */
interface BatchedIdlingVarianceFactor {
    fun describe(): String
    fun adjust(item: BatchedIdlingVariance, value: Double): Double
}

data class MinimumBatchedIdlingVarianceAdjuster(var minimum: Double) : BatchedIdlingVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedIdlingVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedIdlingVarianceCostOffsetAdjuster(var weight: Double) : BatchedIdlingVarianceFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: BatchedIdlingVariance, value: Double): Double = value + item.cost * weight
}

data class TieredBatchedIdlingVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedIdlingVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedIdlingVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedIdlingVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedIdlingVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedIdlingVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameBatchedIdlingVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedIdlingVarianceFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedIdlingVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched idling variance steps in order and keeps a trace of each. */
data class BatchedIdlingVarianceEngine(
    var steps: MutableList<BatchedIdlingVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedIdlingVariance): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedIdlingVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedIdlingVarianceEngine(flags: Registry): BatchedIdlingVarianceEngine {
    return BatchedIdlingVarianceEngine(
        steps = mutableListOf<BatchedIdlingVarianceFactor>(
            MinimumBatchedIdlingVarianceAdjuster(29.1),
            BatchedIdlingVarianceCostOffsetAdjuster(1.64),
            TieredBatchedIdlingVarianceAdjuster(
                mutableListOf<Double>(964.0, 6327.0, 8960.0, 9308.0),
                mutableListOf<Double>(1.07, 1.18, 1.07, 0.85),
            ),
            FlaggedBatchedIdlingVarianceAdjuster(flags, "batched-idling-variance-default", -6.7),
            NameBatchedIdlingVarianceAdjuster(mutableMapOf<String, Double>("online" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 78022.5,
    )
}
