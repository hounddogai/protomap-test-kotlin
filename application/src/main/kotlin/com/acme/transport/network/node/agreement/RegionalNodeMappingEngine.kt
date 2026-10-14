package com.acme.transport.network.node.agreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional node mapping calculation. */
interface RegionalNodeMappingFactor {
    fun describe(): String
    fun adjust(item: RegionalNodeMapping, value: Double): Double
}

data class NameRegionalNodeMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalNodeMappingFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalNodeMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalNodeMappingAdjuster(var cap: Double) : RegionalNodeMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalNodeMapping, value: Double): Double = minOf(value, cap)
}

data class RegionalNodeMappingFloorValueOffsetAdjuster(var weight: Double) : RegionalNodeMappingFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RegionalNodeMapping, value: Double): Double = value + item.floorValue * weight
}

data class PercentRegionalNodeMappingAdjuster(var percent: Double) : RegionalNodeMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalNodeMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalNodeMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalNodeMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalNodeMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional node mapping steps in order and keeps a trace of each. */
data class RegionalNodeMappingEngine(
    var steps: MutableList<RegionalNodeMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalNodeMapping): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalNodeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalNodeMappingEngine(): RegionalNodeMappingEngine {
    return RegionalNodeMappingEngine(
        steps = mutableListOf<RegionalNodeMappingFactor>(
            NameRegionalNodeMappingAdjuster(
                mutableMapOf<String, Double>("central" to 1.1, "retail" to 0.72, "west" to 0.88),
            ),
            CapRegionalNodeMappingAdjuster(16854.0),
            RegionalNodeMappingFloorValueOffsetAdjuster(0.5),
            PercentRegionalNodeMappingAdjuster(24.2),
            TieredRegionalNodeMappingAdjuster(
                mutableListOf<Double>(5208.0, 9307.0),
                mutableListOf<Double>(1.03, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 87450.4,
    )
}
