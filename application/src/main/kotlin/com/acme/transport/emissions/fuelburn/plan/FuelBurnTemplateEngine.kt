package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn template calculation. */
interface FuelBurnTemplateStep {
    fun describe(): String
    fun adjust(item: FuelBurnTemplate, value: Double): Double
}

data class MinimumFuelBurnTemplateAdjuster(var minimum: Double) : FuelBurnTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFuelBurnTemplateAdjuster(var step: Double) : FuelBurnTemplateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedFuelBurnTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fuel burn template steps in order and keeps a trace of each. */
data class FuelBurnTemplateEngine(
    var steps: MutableList<FuelBurnTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnTemplate): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnTemplateStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnTemplateEngine(flags: Registry): FuelBurnTemplateEngine {
    return FuelBurnTemplateEngine(
        steps = mutableListOf<FuelBurnTemplateStep>(
            MinimumFuelBurnTemplateAdjuster(14.8),
            RoundingFuelBurnTemplateAdjuster(0.5),
            FlaggedFuelBurnTemplateAdjuster(flags, "fuel-burn-template-adaptive", -9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 22915.9,
    )
}
