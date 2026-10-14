package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the ev charge agreement calculation. */
interface EvChargeAgreementModifier {
    fun describe(): String
    fun adjust(item: EvChargeAgreement, value: Double): Double
}

data class TieredEvChargeAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedEvChargeAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentEvChargeAgreementAdjuster(var percent: Double) : EvChargeAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumEvChargeAgreementAdjuster(var minimum: Double) : EvChargeAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEvChargeAgreementAdjuster(var cap: Double) : EvChargeAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeAgreement, value: Double): Double = minOf(value, cap)
}

data class LaneEvChargeAgreementAdjuster(var factors: MutableMap<String, Double>) : EvChargeAgreementModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: EvChargeAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the ev charge agreement steps in order and keeps a trace of each. */
data class EvChargeAgreementEngine(
    var steps: MutableList<EvChargeAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeAgreement): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeAgreementEngine(flags: Registry): EvChargeAgreementEngine {
    return EvChargeAgreementEngine(
        steps = mutableListOf<EvChargeAgreementModifier>(
            TieredEvChargeAgreementAdjuster(
                mutableListOf<Double>(1679.0, 8344.0),
                mutableListOf<Double>(0.99, 1.02),
            ),
            FlaggedEvChargeAgreementAdjuster(flags, "ev-charge-agreement-primary", -6.7),
            PercentEvChargeAgreementAdjuster(-18.0),
            MinimumEvChargeAgreementAdjuster(36.1),
            CapEvChargeAgreementAdjuster(22232.0),
            LaneEvChargeAgreementAdjuster(mutableMapOf<String, Double>("central" to 1.27)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 88726.3,
    )
}
