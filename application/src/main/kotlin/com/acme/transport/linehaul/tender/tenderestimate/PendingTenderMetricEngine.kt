package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending tender metric calculation. */
interface PendingTenderMetricFactor {
    fun describe(): String
    fun adjust(item: PendingTenderMetric, value: Double): Double
}

data class TieredPendingTenderMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTenderMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTenderMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingTenderMetricAdjuster(var step: Double) : PendingTenderMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTenderMetric, value: Double): Double = roundTo(value, step)
}

data class CapPendingTenderMetricAdjuster(var cap: Double) : PendingTenderMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTenderMetric, value: Double): Double = minOf(value, cap)
}

data class MinimumPendingTenderMetricAdjuster(var minimum: Double) : PendingTenderMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTenderMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending tender metric steps in order and keeps a trace of each. */
data class PendingTenderMetricEngine(
    var steps: MutableList<PendingTenderMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingTenderMetric): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTenderMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingTenderMetricEngine(): PendingTenderMetricEngine {
    return PendingTenderMetricEngine(
        steps = mutableListOf<PendingTenderMetricFactor>(
            TieredPendingTenderMetricAdjuster(
                mutableListOf<Double>(257.0, 6710.0, 9133.0),
                mutableListOf<Double>(1.04, 0.97, 0.89),
            ),
            RoundingPendingTenderMetricAdjuster(0.1),
            CapPendingTenderMetricAdjuster(5723.0),
            MinimumPendingTenderMetricAdjuster(22.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 19814.0,
    )
}
