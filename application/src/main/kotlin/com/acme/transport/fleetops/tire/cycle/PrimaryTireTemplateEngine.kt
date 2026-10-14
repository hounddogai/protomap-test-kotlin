package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary tire template calculation. */
interface PrimaryTireTemplateStep {
    fun describe(): String
    fun adjust(item: PrimaryTireTemplate, value: Double): Double
}

data class MinimumPrimaryTireTemplateAdjuster(var minimum: Double) : PrimaryTireTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTireTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryTireTemplateAdjuster(var cap: Double) : PrimaryTireTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTireTemplate, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryTireTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTireTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTireTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary tire template steps in order and keeps a trace of each. */
data class PrimaryTireTemplateEngine(
    var steps: MutableList<PrimaryTireTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTireTemplate): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryTireTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTireTemplateEngine(): PrimaryTireTemplateEngine {
    return PrimaryTireTemplateEngine(
        steps = mutableListOf<PrimaryTireTemplateStep>(
            MinimumPrimaryTireTemplateAdjuster(3.0),
            CapPrimaryTireTemplateAdjuster(16291.0),
            TieredPrimaryTireTemplateAdjuster(
                mutableListOf<Double>(3679.0, 4347.0, 6343.0),
                mutableListOf<Double>(1.1, 0.83, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 56113.4,
    )
}
