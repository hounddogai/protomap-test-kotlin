package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.Registry

/** One step of the weighted service area agreement calculation. */
interface WeightedServiceAreaAgreementModifier {
    fun describe(): String
    fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double
}

data class PercentWeightedServiceAreaAgreementAdjuster(
    var percent: Double,
) : WeightedServiceAreaAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedServiceAreaAgreementAdjuster(
    var minimum: Double,
) : WeightedServiceAreaAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedServiceAreaAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedServiceAreaAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleWeightedServiceAreaAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedServiceAreaAgreementModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedServiceAreaAgreementCeilingOffsetAdjuster(
    var weight: Double,
) : WeightedServiceAreaAgreementModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedWeightedServiceAreaAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedServiceAreaAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedServiceAreaAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted service area agreement steps in order and keeps a trace of each. */
data class WeightedServiceAreaAgreementEngine(
    var steps: MutableList<WeightedServiceAreaAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedServiceAreaAgreement): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedServiceAreaAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedServiceAreaAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedServiceAreaAgreementEngine(flags: Registry): WeightedServiceAreaAgreementEngine {
    return WeightedServiceAreaAgreementEngine(
        steps = mutableListOf<WeightedServiceAreaAgreementModifier>(
            PercentWeightedServiceAreaAgreementAdjuster(13.0),
            MinimumWeightedServiceAreaAgreementAdjuster(46.2),
            TieredWeightedServiceAreaAgreementAdjuster(
                mutableListOf<Double>(373.0, 3591.0),
                mutableListOf<Double>(1.16, 0.9),
            ),
            LocaleWeightedServiceAreaAgreementAdjuster(mutableMapOf<String, Double>("online" to 0.95)),
            WeightedServiceAreaAgreementCeilingOffsetAdjuster(0.96),
            FlaggedWeightedServiceAreaAgreementAdjuster(flags, "weighted-service-area-agreement-tiered", -1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 22545.8,
    )
}
