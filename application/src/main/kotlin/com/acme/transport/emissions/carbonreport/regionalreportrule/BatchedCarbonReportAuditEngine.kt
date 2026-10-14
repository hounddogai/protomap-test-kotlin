package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched carbon report audit calculation. */
interface BatchedCarbonReportAuditAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCarbonReportAudit, value: Double): Double
}

data class BatchedCarbonReportAuditLimitOffsetAdjuster(var weight: Double) : BatchedCarbonReportAuditAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: BatchedCarbonReportAudit, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class PercentBatchedCarbonReportAuditAdjuster(var percent: Double) : BatchedCarbonReportAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCarbonReportAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedCarbonReportAuditAdjuster(var step: Double) : BatchedCarbonReportAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCarbonReportAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedCarbonReportAuditAdjuster(var minimum: Double) : BatchedCarbonReportAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCarbonReportAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameBatchedCarbonReportAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCarbonReportAuditAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedCarbonReportAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedCarbonReportAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedCarbonReportAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedCarbonReportAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched carbon report audit steps in order and keeps a trace of each. */
data class BatchedCarbonReportAuditEngine(
    var steps: MutableList<BatchedCarbonReportAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedCarbonReportAudit): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCarbonReportAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedCarbonReportAuditEngine(): BatchedCarbonReportAuditEngine {
    return BatchedCarbonReportAuditEngine(
        steps = mutableListOf<BatchedCarbonReportAuditAdjuster>(
            BatchedCarbonReportAuditLimitOffsetAdjuster(1.56),
            PercentBatchedCarbonReportAuditAdjuster(22.3),
            RoundingBatchedCarbonReportAuditAdjuster(0.25),
            MinimumBatchedCarbonReportAuditAdjuster(8.7),
            NameBatchedCarbonReportAuditAdjuster(mutableMapOf<String, Double>("west" to 1.34)),
            TieredBatchedCarbonReportAuditAdjuster(
                mutableListOf<Double>(2578.0, 2640.0, 9104.0, 9739.0),
                mutableListOf<Double>(1.17, 1.05, 0.92, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 36916.5,
    )
}
