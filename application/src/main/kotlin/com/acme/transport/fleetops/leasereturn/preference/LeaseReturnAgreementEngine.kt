package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lease return agreement calculation. */
interface LeaseReturnAgreementModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnAgreement, value: Double): Double
}

data class PercentLeaseReturnAgreementAdjuster(var percent: Double) : LeaseReturnAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleLeaseReturnAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnAgreementModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LeaseReturnAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLeaseReturnAgreementAdjuster(var cap: Double) : LeaseReturnAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnAgreement, value: Double): Double = minOf(value, cap)
}

data class LeaseReturnAgreementAttemptsOffsetAdjuster(var weight: Double) : LeaseReturnAgreementModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: LeaseReturnAgreement, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class TieredLeaseReturnAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLeaseReturnAgreementAdjuster(var step: Double) : LeaseReturnAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return agreement steps in order and keeps a trace of each. */
data class LeaseReturnAgreementEngine(
    var steps: MutableList<LeaseReturnAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LeaseReturnAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnAgreementEngine(): LeaseReturnAgreementEngine {
    return LeaseReturnAgreementEngine(
        steps = mutableListOf<LeaseReturnAgreementModifier>(
            PercentLeaseReturnAgreementAdjuster(13.7),
            TitleLeaseReturnAgreementAdjuster(mutableMapOf<String, Double>("east" to 1.27)),
            CapLeaseReturnAgreementAdjuster(18896.0),
            LeaseReturnAgreementAttemptsOffsetAdjuster(0.37),
            TieredLeaseReturnAgreementAdjuster(
                mutableListOf<Double>(5997.0, 9409.0, 9432.0),
                mutableListOf<Double>(0.93, 0.95, 1.1),
            ),
            RoundingLeaseReturnAgreementAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 50959.5,
    )
}
