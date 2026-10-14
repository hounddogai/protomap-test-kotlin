package com.acme.transport.emissions.fuelburn.milestone

/** One step of the manual fuel burn threshold calculation. */
interface ManualFuelBurnThresholdFactor {
    fun describe(): String
    fun adjust(item: ManualFuelBurnThreshold, value: Double): Double
}

data class MinimumManualFuelBurnThresholdAdjuster(var minimum: Double) : ManualFuelBurnThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualFuelBurnThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualFuelBurnThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualFuelBurnThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualFuelBurnThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualFuelBurnThresholdAdjuster(var cap: Double) : ManualFuelBurnThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualFuelBurnThreshold, value: Double): Double = minOf(value, cap)
}

data class TitleManualFuelBurnThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualFuelBurnThresholdFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualFuelBurnThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualFuelBurnThresholdAdjuster(var percent: Double) : ManualFuelBurnThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualFuelBurnThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual fuel burn threshold steps in order and keeps a trace of each. */
data class ManualFuelBurnThresholdEngine(
    var steps: MutableList<ManualFuelBurnThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualFuelBurnThreshold): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualFuelBurnThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualFuelBurnThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualFuelBurnThresholdEngine(): ManualFuelBurnThresholdEngine {
    return ManualFuelBurnThresholdEngine(
        steps = mutableListOf<ManualFuelBurnThresholdFactor>(
            MinimumManualFuelBurnThresholdAdjuster(39.4),
            TieredManualFuelBurnThresholdAdjuster(
                mutableListOf<Double>(3816.0, 7151.0, 7965.0, 9935.0),
                mutableListOf<Double>(1.08, 0.82, 0.91, 1.13),
            ),
            CapManualFuelBurnThresholdAdjuster(30996.0),
            TitleManualFuelBurnThresholdAdjuster(
                mutableMapOf<String, Double>("east" to 1.36, "online" to 0.82, "central" to 1.09),
            ),
            PercentManualFuelBurnThresholdAdjuster(21.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 48533.4,
    )
}
