package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped carbon report allocation calculation. */
interface CappedCarbonReportAllocationAdjuster {
    fun describe(): String
    fun adjust(item: CappedCarbonReportAllocation, value: Double): Double
}

data class CappedCarbonReportAllocationOdometerOffsetAdjuster(
    var weight: Double,
) : CappedCarbonReportAllocationAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: CappedCarbonReportAllocation, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class SegmentCappedCarbonReportAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCarbonReportAllocationAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: CappedCarbonReportAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedCarbonReportAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCarbonReportAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCarbonReportAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedCarbonReportAllocationAdjuster(
    var step: Double,
) : CappedCarbonReportAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCarbonReportAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the capped carbon report allocation steps in order and keeps a trace of each. */
data class CappedCarbonReportAllocationEngine(
    var steps: MutableList<CappedCarbonReportAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedCarbonReportAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCarbonReportAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCarbonReportAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedCarbonReportAllocationEngine(flags: Registry): CappedCarbonReportAllocationEngine {
    return CappedCarbonReportAllocationEngine(
        steps = mutableListOf<CappedCarbonReportAllocationAdjuster>(
            CappedCarbonReportAllocationOdometerOffsetAdjuster(1.32),
            SegmentCappedCarbonReportAllocationAdjuster(mutableMapOf<String, Double>("south" to 0.8)),
            FlaggedCappedCarbonReportAllocationAdjuster(flags, "capped-carbon-report-allocation-lenient", 7.2),
            RoundingCappedCarbonReportAllocationAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 35901.0,
    )
}
