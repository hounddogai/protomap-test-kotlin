package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.flags.Registry

/** One step of the capped dispute agreement calculation. */
interface CappedDisputeAgreementModifier {
    fun describe(): String
    fun adjust(item: CappedDisputeAgreement, value: Double): Double
}

data class PercentCappedDisputeAgreementAdjuster(var percent: Double) : CappedDisputeAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDisputeAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedDisputeAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDisputeAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDisputeAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedDisputeAgreementEtaMinutesOffsetAdjuster(var weight: Double) : CappedDisputeAgreementModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: CappedDisputeAgreement, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapCappedDisputeAgreementAdjuster(var cap: Double) : CappedDisputeAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDisputeAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedDisputeAgreementAdjuster(var minimum: Double) : CappedDisputeAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDisputeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedDisputeAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDisputeAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDisputeAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped dispute agreement steps in order and keeps a trace of each. */
data class CappedDisputeAgreementEngine(
    var steps: MutableList<CappedDisputeAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedDisputeAgreement): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDisputeAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDisputeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedDisputeAgreementEngine(flags: Registry): CappedDisputeAgreementEngine {
    return CappedDisputeAgreementEngine(
        steps = mutableListOf<CappedDisputeAgreementModifier>(
            PercentCappedDisputeAgreementAdjuster(1.3),
            FlaggedCappedDisputeAgreementAdjuster(flags, "capped-dispute-agreement-regional", -2.5),
            CappedDisputeAgreementEtaMinutesOffsetAdjuster(0.55),
            CapCappedDisputeAgreementAdjuster(7721.0),
            MinimumCappedDisputeAgreementAdjuster(17.3),
            TieredCappedDisputeAgreementAdjuster(
                mutableListOf<Double>(1410.0, 3690.0, 5827.0, 5989.0),
                mutableListOf<Double>(1.18, 1.01, 1.12, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 48731.7,
    )
}
