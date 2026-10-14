package com.acme.transport.customs.tariff.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff rule calculation. */
interface TariffRuleModifier {
    fun describe(): String
    fun adjust(item: TariffRule, value: Double): Double
}

data class TariffRuleCapacityOffsetAdjuster(var weight: Double) : TariffRuleModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: TariffRule, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumTariffRuleAdjuster(var minimum: Double) : TariffRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleTariffRuleAdjuster(var factors: MutableMap<String, Double>) : TariffRuleModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: TariffRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTariffRuleAdjuster(var cap: Double) : TariffRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffRule, value: Double): Double = minOf(value, cap)
}

data class RoundingTariffRuleAdjuster(var step: Double) : TariffRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffRule, value: Double): Double = roundTo(value, step)
}

/** Runs the tariff rule steps in order and keeps a trace of each. */
data class TariffRuleEngine(
    var steps: MutableList<TariffRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TariffRule): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTariffRuleEngine(): TariffRuleEngine {
    return TariffRuleEngine(
        steps = mutableListOf<TariffRuleModifier>(
            TariffRuleCapacityOffsetAdjuster(0.35),
            MinimumTariffRuleAdjuster(46.6),
            LocaleTariffRuleAdjuster(mutableMapOf<String, Double>("online" to 1.22, "central" to 1.25)),
            CapTariffRuleAdjuster(9029.0),
            RoundingTariffRuleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 60415.0,
    )
}
