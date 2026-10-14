package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary tariff exception calculation. */
interface PrimaryTariffExceptionAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryTariffException, value: Double): Double
}

data class RoundingPrimaryTariffExceptionAdjuster(var step: Double) : PrimaryTariffExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTariffException, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryTariffExceptionAdjuster(var minimum: Double) : PrimaryTariffExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTariffException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitlePrimaryTariffExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTariffExceptionAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryTariffException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryTariffExceptionAdjuster(var percent: Double) : PrimaryTariffExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTariffException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryTariffExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTariffExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTariffException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryTariffExceptionDiscountOffsetAdjuster(var weight: Double) : PrimaryTariffExceptionAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: PrimaryTariffException, value: Double): Double = value + item.discount * weight
}

/** Runs the primary tariff exception steps in order and keeps a trace of each. */
data class PrimaryTariffExceptionEngine(
    var steps: MutableList<PrimaryTariffExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryTariffException): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTariffExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTariffException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryTariffExceptionEngine(): PrimaryTariffExceptionEngine {
    return PrimaryTariffExceptionEngine(
        steps = mutableListOf<PrimaryTariffExceptionAdjuster>(
            RoundingPrimaryTariffExceptionAdjuster(5.0),
            MinimumPrimaryTariffExceptionAdjuster(34.8),
            TitlePrimaryTariffExceptionAdjuster(mutableMapOf<String, Double>("west" to 1.04)),
            PercentPrimaryTariffExceptionAdjuster(-22.3),
            TieredPrimaryTariffExceptionAdjuster(
                mutableListOf<Double>(6100.0, 8952.0, 9752.0),
                mutableListOf<Double>(1.13, 0.86, 1.17),
            ),
            PrimaryTariffExceptionDiscountOffsetAdjuster(0.06),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 37027.6,
    )
}
