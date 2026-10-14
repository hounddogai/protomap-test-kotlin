package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered tariff profile calculation. */
interface TieredTariffProfileFactor {
    fun describe(): String
    fun adjust(item: TieredTariffProfile, value: Double): Double
}

data class PercentTieredTariffProfileAdjuster(var percent: Double) : TieredTariffProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTariffProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredTariffProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredTariffProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredTariffProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredTariffProfileAdjuster(var step: Double) : TieredTariffProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTariffProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredTariffProfileAdjuster(var minimum: Double) : TieredTariffProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTariffProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredTariffProfileAdjuster(var cap: Double) : TieredTariffProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTariffProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered tariff profile steps in order and keeps a trace of each. */
data class TieredTariffProfileEngine(
    var steps: MutableList<TieredTariffProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredTariffProfile): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTariffProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTariffProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredTariffProfileEngine(): TieredTariffProfileEngine {
    return TieredTariffProfileEngine(
        steps = mutableListOf<TieredTariffProfileFactor>(
            PercentTieredTariffProfileAdjuster(-15.0),
            TieredTieredTariffProfileAdjuster(
                mutableListOf<Double>(2920.0, 7569.0),
                mutableListOf<Double>(0.91, 1.17),
            ),
            RoundingTieredTariffProfileAdjuster(0.5),
            MinimumTieredTariffProfileAdjuster(33.3),
            CapTieredTariffProfileAdjuster(25514.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 95941.2,
    )
}
