package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy duty period threshold calculation. */
interface LegacyDutyPeriodThresholdStep {
    fun describe(): String
    fun adjust(item: LegacyDutyPeriodThreshold, value: Double): Double
}

data class RoundingLegacyDutyPeriodThresholdAdjuster(var step: Double) : LegacyDutyPeriodThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDutyPeriodThreshold, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyDutyPeriodThresholdAdjuster(var percent: Double) : LegacyDutyPeriodThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDutyPeriodThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyDutyPeriodThresholdAdjuster(var minimum: Double) : LegacyDutyPeriodThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDutyPeriodThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy duty period threshold steps in order and keeps a trace of each. */
data class LegacyDutyPeriodThresholdEngine(
    var steps: MutableList<LegacyDutyPeriodThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDutyPeriodThreshold): Double {
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

    fun evaluateAll(items: MutableList<LegacyDutyPeriodThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDutyPeriodThresholdEngine(): LegacyDutyPeriodThresholdEngine {
    return LegacyDutyPeriodThresholdEngine(
        steps = mutableListOf<LegacyDutyPeriodThresholdStep>(
            RoundingLegacyDutyPeriodThresholdAdjuster(1.0),
            PercentLegacyDutyPeriodThresholdAdjuster(-14.6),
            MinimumLegacyDutyPeriodThresholdAdjuster(5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 72654.8,
    )
}
