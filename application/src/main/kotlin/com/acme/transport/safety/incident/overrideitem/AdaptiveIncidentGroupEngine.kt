package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive incident group calculation. */
interface AdaptiveIncidentGroupAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveIncidentGroup, value: Double): Double
}

data class TieredAdaptiveIncidentGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveIncidentGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveIncidentGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameAdaptiveIncidentGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveIncidentGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveIncidentGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveIncidentGroupAdjuster(var percent: Double) : AdaptiveIncidentGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveIncidentGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive incident group steps in order and keeps a trace of each. */
data class AdaptiveIncidentGroupEngine(
    var steps: MutableList<AdaptiveIncidentGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveIncidentGroup): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveIncidentGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveIncidentGroupEngine(): AdaptiveIncidentGroupEngine {
    return AdaptiveIncidentGroupEngine(
        steps = mutableListOf<AdaptiveIncidentGroupAdjuster>(
            TieredAdaptiveIncidentGroupAdjuster(
                mutableListOf<Double>(1609.0, 5717.0, 7824.0),
                mutableListOf<Double>(1.13, 0.82, 1.15),
            ),
            NameAdaptiveIncidentGroupAdjuster(
                mutableMapOf<String, Double>("west" to 0.79, "retail" to 1.01, "north" to 0.97),
            ),
            PercentAdaptiveIncidentGroupAdjuster(-3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 46754.6,
    )
}
