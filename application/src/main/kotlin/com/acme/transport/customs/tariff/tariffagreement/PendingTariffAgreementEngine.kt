package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending tariff agreement calculation. */
interface PendingTariffAgreementAdjuster {
    fun describe(): String
    fun adjust(item: PendingTariffAgreement, value: Double): Double
}

data class CapPendingTariffAgreementAdjuster(var cap: Double) : PendingTariffAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTariffAgreement, value: Double): Double = minOf(value, cap)
}

data class PendingTariffAgreementStopCountOffsetAdjuster(var weight: Double) : PendingTariffAgreementAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PendingTariffAgreement, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class TieredPendingTariffAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTariffAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTariffAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingTariffAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTariffAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTariffAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingTariffAgreementAdjuster(var step: Double) : PendingTariffAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTariffAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the pending tariff agreement steps in order and keeps a trace of each. */
data class PendingTariffAgreementEngine(
    var steps: MutableList<PendingTariffAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingTariffAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTariffAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTariffAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingTariffAgreementEngine(flags: Registry): PendingTariffAgreementEngine {
    return PendingTariffAgreementEngine(
        steps = mutableListOf<PendingTariffAgreementAdjuster>(
            CapPendingTariffAgreementAdjuster(10953.0),
            PendingTariffAgreementStopCountOffsetAdjuster(0.93),
            TieredPendingTariffAgreementAdjuster(
                mutableListOf<Double>(1241.0, 4975.0),
                mutableListOf<Double>(1.07, 0.95),
            ),
            FlaggedPendingTariffAgreementAdjuster(flags, "pending-tariff-agreement-pending", 9.7),
            RoundingPendingTariffAgreementAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 67025.7,
    )
}
