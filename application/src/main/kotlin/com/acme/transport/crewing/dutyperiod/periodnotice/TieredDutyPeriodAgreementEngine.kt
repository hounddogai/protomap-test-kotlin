package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered duty period agreement calculation. */
interface TieredDutyPeriodAgreementAdjuster {
    fun describe(): String
    fun adjust(item: TieredDutyPeriodAgreement, value: Double): Double
}

data class PercentTieredDutyPeriodAgreementAdjuster(var percent: Double) : TieredDutyPeriodAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDutyPeriodAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDutyPeriodAgreementDistanceKmOffsetAdjuster(
    var weight: Double,
) : TieredDutyPeriodAgreementAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: TieredDutyPeriodAgreement, value: Double): Double = value + item.distanceKm * weight
}

data class TieredTieredDutyPeriodAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDutyPeriodAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDutyPeriodAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredDutyPeriodAgreementAdjuster(var step: Double) : TieredDutyPeriodAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDutyPeriodAgreement, value: Double): Double = roundTo(value, step)
}

data class RegionTieredDutyPeriodAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDutyPeriodAgreementAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: TieredDutyPeriodAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered duty period agreement steps in order and keeps a trace of each. */
data class TieredDutyPeriodAgreementEngine(
    var steps: MutableList<TieredDutyPeriodAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredDutyPeriodAgreement): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDutyPeriodAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDutyPeriodAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredDutyPeriodAgreementEngine(): TieredDutyPeriodAgreementEngine {
    return TieredDutyPeriodAgreementEngine(
        steps = mutableListOf<TieredDutyPeriodAgreementAdjuster>(
            PercentTieredDutyPeriodAgreementAdjuster(15.8),
            TieredDutyPeriodAgreementDistanceKmOffsetAdjuster(1.36),
            TieredTieredDutyPeriodAgreementAdjuster(
                mutableListOf<Double>(929.0, 2642.0, 4694.0, 9867.0),
                mutableListOf<Double>(1.1, 1.11, 0.89, 1.05),
            ),
            RoundingTieredDutyPeriodAgreementAdjuster(0.1),
            RegionTieredDutyPeriodAgreementAdjuster(
                mutableMapOf<String, Double>("online" to 1.37, "south" to 1.22, "east" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 37024.5,
    )
}
