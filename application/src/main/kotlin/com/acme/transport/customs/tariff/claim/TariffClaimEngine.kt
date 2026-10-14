package com.acme.transport.customs.tariff.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff claim calculation. */
interface TariffClaimModifier {
    fun describe(): String
    fun adjust(item: TariffClaim, value: Double): Double
}

data class RoundingTariffClaimAdjuster(var step: Double) : TariffClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumTariffClaimAdjuster(var minimum: Double) : TariffClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelTariffClaimAdjuster(var factors: MutableMap<String, Double>) : TariffClaimModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TariffClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTariffClaimAdjuster(var cap: Double) : TariffClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffClaim, value: Double): Double = minOf(value, cap)
}

data class TieredTariffClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTariffClaimAdjuster(var percent: Double) : TariffClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tariff claim steps in order and keeps a trace of each. */
data class TariffClaimEngine(
    var steps: MutableList<TariffClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffClaim): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffClaimEngine(): TariffClaimEngine {
    return TariffClaimEngine(
        steps = mutableListOf<TariffClaimModifier>(
            RoundingTariffClaimAdjuster(0.1),
            MinimumTariffClaimAdjuster(37.2),
            LabelTariffClaimAdjuster(mutableMapOf<String, Double>("central" to 1.22, "north" to 1.26)),
            CapTariffClaimAdjuster(14702.0),
            TieredTariffClaimAdjuster(
                mutableListOf<Double>(249.0, 2119.0, 5743.0),
                mutableListOf<Double>(0.96, 1.08, 0.99),
            ),
            PercentTariffClaimAdjuster(-3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 44023.1,
    )
}
