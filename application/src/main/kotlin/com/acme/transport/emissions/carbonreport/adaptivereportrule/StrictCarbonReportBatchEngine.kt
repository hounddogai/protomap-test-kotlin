package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict carbon report batch calculation. */
interface StrictCarbonReportBatchAdjuster {
    fun describe(): String
    fun adjust(item: StrictCarbonReportBatch, value: Double): Double
}

data class StrictCarbonReportBatchPalletsOffsetAdjuster(var weight: Double) : StrictCarbonReportBatchAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: StrictCarbonReportBatch, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class ChannelStrictCarbonReportBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictCarbonReportBatchAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: StrictCarbonReportBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictCarbonReportBatchAdjuster(var minimum: Double) : StrictCarbonReportBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCarbonReportBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict carbon report batch steps in order and keeps a trace of each. */
data class StrictCarbonReportBatchEngine(
    var steps: MutableList<StrictCarbonReportBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictCarbonReportBatch): Double {
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

    fun evaluateAll(items: MutableList<StrictCarbonReportBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictCarbonReportBatchEngine(): StrictCarbonReportBatchEngine {
    return StrictCarbonReportBatchEngine(
        steps = mutableListOf<StrictCarbonReportBatchAdjuster>(
            StrictCarbonReportBatchPalletsOffsetAdjuster(0.41),
            ChannelStrictCarbonReportBatchAdjuster(
                mutableMapOf<String, Double>("central" to 0.92, "east" to 1.37, "retail" to 0.73),
            ),
            MinimumStrictCarbonReportBatchAdjuster(22.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 50844.3,
    )
}
