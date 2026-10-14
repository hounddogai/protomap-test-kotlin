package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional fuel burn record calculation. */
interface RegionalFuelBurnRecordAdjuster {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnRecord, value: Double): Double
}

data class TieredRegionalFuelBurnRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelBurnRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelBurnRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalFuelBurnRecordAdjuster(var cap: Double) : RegionalFuelBurnRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelBurnRecord, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalFuelBurnRecordAdjuster(var percent: Double) : RegionalFuelBurnRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalFuelBurnRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalFuelBurnRecordAdjuster(var step: Double) : RegionalFuelBurnRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalFuelBurnRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the regional fuel burn record steps in order and keeps a trace of each. */
data class RegionalFuelBurnRecordEngine(
    var steps: MutableList<RegionalFuelBurnRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalFuelBurnRecord): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelBurnRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelBurnRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnRecordEngine(): RegionalFuelBurnRecordEngine {
    return RegionalFuelBurnRecordEngine(
        steps = mutableListOf<RegionalFuelBurnRecordAdjuster>(
            TieredRegionalFuelBurnRecordAdjuster(
                mutableListOf<Double>(2002.0, 5919.0, 6512.0),
                mutableListOf<Double>(1.09, 1.08, 0.96),
            ),
            CapRegionalFuelBurnRecordAdjuster(40485.0),
            PercentRegionalFuelBurnRecordAdjuster(24.6),
            RoundingRegionalFuelBurnRecordAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 73484.5,
    )
}
