package com.acme.transport.customs.tariff.preference

/** One step of the capped tariff claim calculation. */
interface CappedTariffClaimModifier {
    fun describe(): String
    fun adjust(item: CappedTariffClaim, value: Double): Double
}

data class MinimumCappedTariffClaimAdjuster(var minimum: Double) : CappedTariffClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTariffClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryCappedTariffClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTariffClaimModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: CappedTariffClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedTariffClaimAdjuster(var cap: Double) : CappedTariffClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedTariffClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the capped tariff claim steps in order and keeps a trace of each. */
data class CappedTariffClaimEngine(
    var steps: MutableList<CappedTariffClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedTariffClaim): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedTariffClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedTariffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedTariffClaimEngine(): CappedTariffClaimEngine {
    return CappedTariffClaimEngine(
        steps = mutableListOf<CappedTariffClaimModifier>(
            MinimumCappedTariffClaimAdjuster(6.6),
            CountryCappedTariffClaimAdjuster(mutableMapOf<String, Double>("north" to 1.33)),
            CapCappedTariffClaimAdjuster(14084.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 89025.9,
    )
}
