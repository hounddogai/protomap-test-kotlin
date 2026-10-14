package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered lane plan threshold calculation. */
interface TieredLanePlanThresholdAdjuster {
    fun describe(): String
    fun adjust(item: TieredLanePlanThreshold, value: Double): Double
}

data class TieredTieredLanePlanThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredLanePlanThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredLanePlanThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameTieredLanePlanThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredLanePlanThresholdAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredLanePlanThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredLanePlanThresholdAdjuster(var percent: Double) : TieredLanePlanThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredLanePlanThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered lane plan threshold steps in order and keeps a trace of each. */
data class TieredLanePlanThresholdEngine(
    var steps: MutableList<TieredLanePlanThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredLanePlanThreshold): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredLanePlanThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredLanePlanThresholdEngine(): TieredLanePlanThresholdEngine {
    return TieredLanePlanThresholdEngine(
        steps = mutableListOf<TieredLanePlanThresholdAdjuster>(
            TieredTieredLanePlanThresholdAdjuster(
                mutableListOf<Double>(19.0, 437.0),
                mutableListOf<Double>(0.91, 1.11),
            ),
            NameTieredLanePlanThresholdAdjuster(mutableMapOf<String, Double>("north" to 0.74)),
            PercentTieredLanePlanThresholdAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 78925.9,
    )
}
