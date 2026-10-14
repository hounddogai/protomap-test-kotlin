package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling fuel burn agreement calculation. */
interface RollingFuelBurnAgreementModifier {
    fun describe(): String
    fun adjust(item: RollingFuelBurnAgreement, value: Double): Double
}

data class FlaggedRollingFuelBurnAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFuelBurnAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFuelBurnAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingFuelBurnAgreementAdjuster(var cap: Double) : RollingFuelBurnAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelBurnAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingFuelBurnAgreementAdjuster(var minimum: Double) : RollingFuelBurnAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingFuelBurnAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingFuelBurnAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFuelBurnAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFuelBurnAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingFuelBurnAgreementAdjuster(var step: Double) : RollingFuelBurnAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFuelBurnAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling fuel burn agreement steps in order and keeps a trace of each. */
data class RollingFuelBurnAgreementEngine(
    var steps: MutableList<RollingFuelBurnAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingFuelBurnAgreement): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFuelBurnAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingFuelBurnAgreementEngine(flags: Registry): RollingFuelBurnAgreementEngine {
    return RollingFuelBurnAgreementEngine(
        steps = mutableListOf<RollingFuelBurnAgreementModifier>(
            FlaggedRollingFuelBurnAgreementAdjuster(flags, "rolling-fuel-burn-agreement-global", 5.6),
            CapRollingFuelBurnAgreementAdjuster(38782.0),
            MinimumRollingFuelBurnAgreementAdjuster(26.8),
            TieredRollingFuelBurnAgreementAdjuster(
                mutableListOf<Double>(967.0, 2468.0, 3949.0, 5906.0),
                mutableListOf<Double>(0.9, 1.03, 1.01, 1.09),
            ),
            RoundingRollingFuelBurnAgreementAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 4409.0,
    )
}
