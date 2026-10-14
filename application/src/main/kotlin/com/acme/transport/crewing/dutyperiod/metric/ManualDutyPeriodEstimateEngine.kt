package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual duty period estimate calculation. */
interface ManualDutyPeriodEstimateStep {
    fun describe(): String
    fun adjust(item: ManualDutyPeriodEstimate, value: Double): Double
}

data class DepotManualDutyPeriodEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDutyPeriodEstimateStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualDutyPeriodEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualDutyPeriodEstimateAdjuster(var cap: Double) : ManualDutyPeriodEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDutyPeriodEstimate, value: Double): Double = minOf(value, cap)
}

data class PercentManualDutyPeriodEstimateAdjuster(var percent: Double) : ManualDutyPeriodEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDutyPeriodEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual duty period estimate steps in order and keeps a trace of each. */
data class ManualDutyPeriodEstimateEngine(
    var steps: MutableList<ManualDutyPeriodEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDutyPeriodEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDutyPeriodEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDutyPeriodEstimateEngine(): ManualDutyPeriodEstimateEngine {
    return ManualDutyPeriodEstimateEngine(
        steps = mutableListOf<ManualDutyPeriodEstimateStep>(
            DepotManualDutyPeriodEstimateAdjuster(
                mutableMapOf<String, Double>("central" to 1.09, "south" to 0.89),
            ),
            CapManualDutyPeriodEstimateAdjuster(39380.0),
            PercentManualDutyPeriodEstimateAdjuster(-10.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 72740.6,
    )
}
