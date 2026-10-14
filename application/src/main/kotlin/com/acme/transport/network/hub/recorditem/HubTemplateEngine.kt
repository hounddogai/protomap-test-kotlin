package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub template calculation. */
interface HubTemplateStep {
    fun describe(): String
    fun adjust(item: HubTemplate, value: Double): Double
}

data class TitleHubTemplateAdjuster(var factors: MutableMap<String, Double>) : HubTemplateStep {
    override fun describe(): String = "by title"

    override fun adjust(item: HubTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredHubTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumHubTemplateAdjuster(var minimum: Double) : HubTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hub template steps in order and keeps a trace of each. */
data class HubTemplateEngine(
    var steps: MutableList<HubTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubTemplate): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubTemplateEngine(): HubTemplateEngine {
    return HubTemplateEngine(
        steps = mutableListOf<HubTemplateStep>(
            TitleHubTemplateAdjuster(mutableMapOf<String, Double>("south" to 1.01)),
            TieredHubTemplateAdjuster(
                mutableListOf<Double>(3977.0, 7965.0, 8111.0),
                mutableListOf<Double>(0.88, 0.84, 0.85),
            ),
            MinimumHubTemplateAdjuster(48.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 34335.6,
    )
}
