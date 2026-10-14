package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling emission factor agreement calculation. */
interface RollingEmissionFactorAgreementModifier {
    fun describe(): String
    fun adjust(item: RollingEmissionFactorAgreement, value: Double): Double
}

data class RollingEmissionFactorAgreementMinimumOffsetAdjuster(
    var weight: Double,
) : RollingEmissionFactorAgreementModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: RollingEmissionFactorAgreement, value: Double): Double = value + item.minimum * weight
}

data class FlaggedRollingEmissionFactorAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingEmissionFactorAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingEmissionFactorAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingEmissionFactorAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingEmissionFactorAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingEmissionFactorAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingEmissionFactorAgreementAdjuster(
    var step: Double,
) : RollingEmissionFactorAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingEmissionFactorAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling emission factor agreement steps in order and keeps a trace of each. */
data class RollingEmissionFactorAgreementEngine(
    var steps: MutableList<RollingEmissionFactorAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingEmissionFactorAgreement): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingEmissionFactorAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingEmissionFactorAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingEmissionFactorAgreementEngine(flags: Registry): RollingEmissionFactorAgreementEngine {
    return RollingEmissionFactorAgreementEngine(
        steps = mutableListOf<RollingEmissionFactorAgreementModifier>(
            RollingEmissionFactorAgreementMinimumOffsetAdjuster(1.1),
            FlaggedRollingEmissionFactorAgreementAdjuster(
                flags,
                "rolling-emission-factor-agreement-weighted",
                -1.7,
            ),
            TieredRollingEmissionFactorAgreementAdjuster(
                mutableListOf<Double>(5361.0, 6208.0, 7972.0),
                mutableListOf<Double>(1.01, 0.98, 1.2),
            ),
            RoundingRollingEmissionFactorAgreementAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 87747.2,
    )
}
