package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default availability agreement calculation. */
interface DefaultAvailabilityAgreementStep {
    fun describe(): String
    fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double
}

data class TieredDefaultAvailabilityAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAvailabilityAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultAvailabilityAgreementAdjuster(var minimum: Double) : DefaultAvailabilityAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultAvailabilityAgreementAdjuster(var percent: Double) : DefaultAvailabilityAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultAvailabilityAgreementAdjuster(var step: Double) : DefaultAvailabilityAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultAvailabilityAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAvailabilityAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultAvailabilityAgreementAdjuster(var cap: Double) : DefaultAvailabilityAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAvailabilityAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the default availability agreement steps in order and keeps a trace of each. */
data class DefaultAvailabilityAgreementEngine(
    var steps: MutableList<DefaultAvailabilityAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultAvailabilityAgreement): Double {
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

    fun evaluateAll(items: MutableList<DefaultAvailabilityAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultAvailabilityAgreementEngine(flags: Registry): DefaultAvailabilityAgreementEngine {
    return DefaultAvailabilityAgreementEngine(
        steps = mutableListOf<DefaultAvailabilityAgreementStep>(
            TieredDefaultAvailabilityAgreementAdjuster(
                mutableListOf<Double>(3135.0, 8332.0),
                mutableListOf<Double>(0.96, 0.95),
            ),
            MinimumDefaultAvailabilityAgreementAdjuster(46.1),
            PercentDefaultAvailabilityAgreementAdjuster(10.9),
            RoundingDefaultAvailabilityAgreementAdjuster(0.25),
            FlaggedDefaultAvailabilityAgreementAdjuster(flags, "default-availability-agreement-pending", 1.7),
            CapDefaultAvailabilityAgreementAdjuster(35651.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 59998.0,
    )
}
