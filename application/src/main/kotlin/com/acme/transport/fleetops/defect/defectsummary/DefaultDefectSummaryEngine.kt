package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default defect summary calculation. */
interface DefaultDefectSummaryAdjuster {
    fun describe(): String
    fun adjust(item: DefaultDefectSummary, value: Double): Double
}

data class MinimumDefaultDefectSummaryAdjuster(var minimum: Double) : DefaultDefectSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDefectSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultDefectSummaryAdjuster(var percent: Double) : DefaultDefectSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDefectSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultDefectSummaryAdjuster(var step: Double) : DefaultDefectSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDefectSummary, value: Double): Double = roundTo(value, step)
}

data class DefaultDefectSummaryRateOffsetAdjuster(var weight: Double) : DefaultDefectSummaryAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: DefaultDefectSummary, value: Double): Double = value + item.rate * weight
}

data class CapDefaultDefectSummaryAdjuster(var cap: Double) : DefaultDefectSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDefectSummary, value: Double): Double = minOf(value, cap)
}

data class LabelDefaultDefectSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDefectSummaryAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultDefectSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default defect summary steps in order and keeps a trace of each. */
data class DefaultDefectSummaryEngine(
    var steps: MutableList<DefaultDefectSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultDefectSummary): Double {
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

    fun evaluateAll(items: MutableList<DefaultDefectSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultDefectSummaryEngine(): DefaultDefectSummaryEngine {
    return DefaultDefectSummaryEngine(
        steps = mutableListOf<DefaultDefectSummaryAdjuster>(
            MinimumDefaultDefectSummaryAdjuster(17.8),
            PercentDefaultDefectSummaryAdjuster(22.7),
            RoundingDefaultDefectSummaryAdjuster(0.05),
            DefaultDefectSummaryRateOffsetAdjuster(0.79),
            CapDefaultDefectSummaryAdjuster(40768.0),
            LabelDefaultDefectSummaryAdjuster(
                mutableMapOf<String, Double>("south" to 1.05, "north" to 1.11, "west" to 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 51683.4,
    )
}
