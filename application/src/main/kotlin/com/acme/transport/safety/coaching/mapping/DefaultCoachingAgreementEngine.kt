package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default coaching agreement calculation. */
interface DefaultCoachingAgreementFactor {
    fun describe(): String
    fun adjust(item: DefaultCoachingAgreement, value: Double): Double
}

data class DefaultCoachingAgreementMaximumOffsetAdjuster(var weight: Double) : DefaultCoachingAgreementFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DefaultCoachingAgreement, value: Double): Double = value + item.maximum * weight
}

data class PercentDefaultCoachingAgreementAdjuster(var percent: Double) : DefaultCoachingAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCoachingAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultCoachingAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCoachingAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCoachingAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDefaultCoachingAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCoachingAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCoachingAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultCoachingAgreementAdjuster(var step: Double) : DefaultCoachingAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCoachingAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultCoachingAgreementAdjuster(var minimum: Double) : DefaultCoachingAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCoachingAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default coaching agreement steps in order and keeps a trace of each. */
data class DefaultCoachingAgreementEngine(
    var steps: MutableList<DefaultCoachingAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultCoachingAgreement): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCoachingAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCoachingAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultCoachingAgreementEngine(flags: Registry): DefaultCoachingAgreementEngine {
    return DefaultCoachingAgreementEngine(
        steps = mutableListOf<DefaultCoachingAgreementFactor>(
            DefaultCoachingAgreementMaximumOffsetAdjuster(1.4),
            PercentDefaultCoachingAgreementAdjuster(1.3),
            FlaggedDefaultCoachingAgreementAdjuster(flags, "default-coaching-agreement-manual", 4.3),
            TieredDefaultCoachingAgreementAdjuster(
                mutableListOf<Double>(1379.0, 2747.0, 4184.0, 7771.0),
                mutableListOf<Double>(0.93, 1.03, 1.03, 0.99),
            ),
            RoundingDefaultCoachingAgreementAdjuster(0.5),
            MinimumDefaultCoachingAgreementAdjuster(33.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 3998.7,
    )
}
