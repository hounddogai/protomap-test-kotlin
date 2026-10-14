package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default route offset agreement calculation. */
interface DefaultRouteOffsetAgreementStep {
    fun describe(): String
    fun adjust(item: DefaultRouteOffsetAgreement, value: Double): Double
}

data class DefaultRouteOffsetAgreementRateOffsetAdjuster(var weight: Double) : DefaultRouteOffsetAgreementStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: DefaultRouteOffsetAgreement, value: Double): Double = value + item.rate * weight
}

data class RoundingDefaultRouteOffsetAgreementAdjuster(var step: Double) : DefaultRouteOffsetAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRouteOffsetAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultRouteOffsetAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRouteOffsetAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRouteOffsetAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultRouteOffsetAgreementAdjuster(var cap: Double) : DefaultRouteOffsetAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRouteOffsetAgreement, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultRouteOffsetAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRouteOffsetAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRouteOffsetAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default route offset agreement steps in order and keeps a trace of each. */
data class DefaultRouteOffsetAgreementEngine(
    var steps: MutableList<DefaultRouteOffsetAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultRouteOffsetAgreement): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRouteOffsetAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultRouteOffsetAgreementEngine(flags: Registry): DefaultRouteOffsetAgreementEngine {
    return DefaultRouteOffsetAgreementEngine(
        steps = mutableListOf<DefaultRouteOffsetAgreementStep>(
            DefaultRouteOffsetAgreementRateOffsetAdjuster(1.43),
            RoundingDefaultRouteOffsetAgreementAdjuster(0.1),
            FlaggedDefaultRouteOffsetAgreementAdjuster(flags, "default-route-offset-agreement-strict", -4.5),
            CapDefaultRouteOffsetAgreementAdjuster(41913.0),
            TieredDefaultRouteOffsetAgreementAdjuster(
                mutableListOf<Double>(2440.0, 6703.0),
                mutableListOf<Double>(1.08, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 13403.2,
    )
}
