package com.acme.transport.emissions.fuelburn.manifest

/** One step of the fuel burn manifest calculation. */
interface FuelBurnManifestModifier {
    fun describe(): String
    fun adjust(item: FuelBurnManifest, value: Double): Double
}

data class CapFuelBurnManifestAdjuster(var cap: Double) : FuelBurnManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnManifest, value: Double): Double = minOf(value, cap)
}

data class LabelFuelBurnManifestAdjuster(var factors: MutableMap<String, Double>) : FuelBurnManifestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: FuelBurnManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFuelBurnManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FuelBurnManifestAttemptsOffsetAdjuster(var weight: Double) : FuelBurnManifestModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: FuelBurnManifest, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class PercentFuelBurnManifestAdjuster(var percent: Double) : FuelBurnManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fuel burn manifest steps in order and keeps a trace of each. */
data class FuelBurnManifestEngine(
    var steps: MutableList<FuelBurnManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnManifestEngine(): FuelBurnManifestEngine {
    return FuelBurnManifestEngine(
        steps = mutableListOf<FuelBurnManifestModifier>(
            CapFuelBurnManifestAdjuster(38531.0),
            LabelFuelBurnManifestAdjuster(
                mutableMapOf<String, Double>("north" to 0.8, "south" to 1.24, "east" to 0.93),
            ),
            TieredFuelBurnManifestAdjuster(
                mutableListOf<Double>(3127.0, 5623.0, 6620.0),
                mutableListOf<Double>(0.87, 0.92, 1.04),
            ),
            FuelBurnManifestAttemptsOffsetAdjuster(0.7),
            PercentFuelBurnManifestAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 56680.2,
    )
}
