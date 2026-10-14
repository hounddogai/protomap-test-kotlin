package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped lane plan threshold calculation. */
interface CappedLanePlanThresholdModifier {
    fun describe(): String
    fun adjust(item: CappedLanePlanThreshold, value: Double): Double
}

data class TieredCappedLanePlanThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLanePlanThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLanePlanThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedLanePlanThresholdRatioOffsetAdjuster(var weight: Double) : CappedLanePlanThresholdModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: CappedLanePlanThreshold, value: Double): Double = value + item.ratio * weight
}

data class PercentCappedLanePlanThresholdAdjuster(var percent: Double) : CappedLanePlanThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedLanePlanThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped lane plan threshold steps in order and keeps a trace of each. */
data class CappedLanePlanThresholdEngine(
    var steps: MutableList<CappedLanePlanThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedLanePlanThreshold): Double {
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

    fun evaluateAll(items: MutableList<CappedLanePlanThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedLanePlanThresholdEngine(): CappedLanePlanThresholdEngine {
    return CappedLanePlanThresholdEngine(
        steps = mutableListOf<CappedLanePlanThresholdModifier>(
            TieredCappedLanePlanThresholdAdjuster(
                mutableListOf<Double>(3336.0, 6592.0, 9347.0),
                mutableListOf<Double>(0.97, 0.82, 0.82),
            ),
            CappedLanePlanThresholdRatioOffsetAdjuster(1.5),
            PercentCappedLanePlanThresholdAdjuster(13.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 77078.3,
    )
}
