package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.flags.Registry

/** One step of the default ev charge claim calculation. */
interface DefaultEvChargeClaimAdjuster {
    fun describe(): String
    fun adjust(item: DefaultEvChargeClaim, value: Double): Double
}

data class TieredDefaultEvChargeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultEvChargeClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultEvChargeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultEvChargeClaimRevisionOffsetAdjuster(var weight: Double) : DefaultEvChargeClaimAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: DefaultEvChargeClaim, value: Double): Double = value + item.revision.toDouble() * weight
}

data class FlaggedDefaultEvChargeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultEvChargeClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultEvChargeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default ev charge claim steps in order and keeps a trace of each. */
data class DefaultEvChargeClaimEngine(
    var steps: MutableList<DefaultEvChargeClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultEvChargeClaim): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultEvChargeClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultEvChargeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultEvChargeClaimEngine(flags: Registry): DefaultEvChargeClaimEngine {
    return DefaultEvChargeClaimEngine(
        steps = mutableListOf<DefaultEvChargeClaimAdjuster>(
            TieredDefaultEvChargeClaimAdjuster(
                mutableListOf<Double>(3332.0, 3847.0, 5368.0, 9525.0),
                mutableListOf<Double>(1.02, 0.9, 1.04, 1.12),
            ),
            DefaultEvChargeClaimRevisionOffsetAdjuster(0.05),
            FlaggedDefaultEvChargeClaimAdjuster(flags, "default-ev-charge-claim-lenient", 9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 65913.5,
    )
}
