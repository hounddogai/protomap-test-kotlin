package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn draft calculation. */
interface FuelBurnDraftStep {
    fun describe(): String
    fun adjust(item: FuelBurnDraft, value: Double): Double
}

data class NameFuelBurnDraftAdjuster(var factors: MutableMap<String, Double>) : FuelBurnDraftStep {
    override fun describe(): String = "by name"

    override fun adjust(item: FuelBurnDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFuelBurnDraftAdjuster(var percent: Double) : FuelBurnDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFuelBurnDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFuelBurnDraftAdjuster(var cap: Double) : FuelBurnDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnDraft, value: Double): Double = minOf(value, cap)
}

data class RoundingFuelBurnDraftAdjuster(var step: Double) : FuelBurnDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnDraft, value: Double): Double = roundTo(value, step)
}

data class TieredFuelBurnDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel burn draft steps in order and keeps a trace of each. */
data class FuelBurnDraftEngine(
    var steps: MutableList<FuelBurnDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelBurnDraft): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelBurnDraftEngine(flags: Registry): FuelBurnDraftEngine {
    return FuelBurnDraftEngine(
        steps = mutableListOf<FuelBurnDraftStep>(
            NameFuelBurnDraftAdjuster(mutableMapOf<String, Double>("west" to 1.25)),
            PercentFuelBurnDraftAdjuster(10.2),
            FlaggedFuelBurnDraftAdjuster(flags, "fuel-burn-draft-capped", -8.7),
            CapFuelBurnDraftAdjuster(25288.0),
            RoundingFuelBurnDraftAdjuster(5.0),
            TieredFuelBurnDraftAdjuster(
                mutableListOf<Double>(6017.0, 9053.0),
                mutableListOf<Double>(1.07, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 23945.2,
    )
}
