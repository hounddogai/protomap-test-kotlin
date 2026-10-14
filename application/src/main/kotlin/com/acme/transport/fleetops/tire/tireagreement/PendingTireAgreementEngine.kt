package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.flags.Registry

/** One step of the pending tire agreement calculation. */
interface PendingTireAgreementModifier {
    fun describe(): String
    fun adjust(item: PendingTireAgreement, value: Double): Double
}

data class MinimumPendingTireAgreementAdjuster(var minimum: Double) : PendingTireAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTireAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingTireAgreementAdjuster(var percent: Double) : PendingTireAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingTireAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingTireAgreementAdjuster(var cap: Double) : PendingTireAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTireAgreement, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingTireAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTireAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTireAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingTireAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTireAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTireAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending tire agreement steps in order and keeps a trace of each. */
data class PendingTireAgreementEngine(
    var steps: MutableList<PendingTireAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingTireAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTireAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTireAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingTireAgreementEngine(flags: Registry): PendingTireAgreementEngine {
    return PendingTireAgreementEngine(
        steps = mutableListOf<PendingTireAgreementModifier>(
            MinimumPendingTireAgreementAdjuster(32.1),
            PercentPendingTireAgreementAdjuster(2.2),
            CapPendingTireAgreementAdjuster(28926.0),
            FlaggedPendingTireAgreementAdjuster(flags, "pending-tire-agreement-manual", -1.8),
            TieredPendingTireAgreementAdjuster(
                mutableListOf<Double>(6060.0, 9241.0),
                mutableListOf<Double>(1.04, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 45227.6,
    )
}
