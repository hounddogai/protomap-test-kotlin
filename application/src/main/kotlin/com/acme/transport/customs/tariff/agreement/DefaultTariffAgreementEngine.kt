package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default tariff agreement calculation. */
interface DefaultTariffAgreementModifier {
    fun describe(): String
    fun adjust(item: DefaultTariffAgreement, value: Double): Double
}

data class MinimumDefaultTariffAgreementAdjuster(var minimum: Double) : DefaultTariffAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTariffAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultTariffAgreementLimitOffsetAdjuster(var weight: Double) : DefaultTariffAgreementModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: DefaultTariffAgreement, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TieredDefaultTariffAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTariffAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTariffAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryDefaultTariffAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTariffAgreementModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: DefaultTariffAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default tariff agreement steps in order and keeps a trace of each. */
data class DefaultTariffAgreementEngine(
    var steps: MutableList<DefaultTariffAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultTariffAgreement): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultTariffAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultTariffAgreementEngine(): DefaultTariffAgreementEngine {
    return DefaultTariffAgreementEngine(
        steps = mutableListOf<DefaultTariffAgreementModifier>(
            MinimumDefaultTariffAgreementAdjuster(37.2),
            DefaultTariffAgreementLimitOffsetAdjuster(0.91),
            TieredDefaultTariffAgreementAdjuster(
                mutableListOf<Double>(4610.0, 6493.0),
                mutableListOf<Double>(0.92, 0.89),
            ),
            CountryDefaultTariffAgreementAdjuster(
                mutableMapOf<String, Double>("west" to 1.35, "online" to 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 24931.3,
    )
}
