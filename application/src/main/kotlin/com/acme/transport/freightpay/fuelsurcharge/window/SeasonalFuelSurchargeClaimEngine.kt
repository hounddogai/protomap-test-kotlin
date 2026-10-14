package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal fuel surcharge claim calculation. */
interface SeasonalFuelSurchargeClaimFactor {
    fun describe(): String
    fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double
}

data class SeasonalFuelSurchargeClaimSequenceOffsetAdjuster(
    var weight: Double,
) : SeasonalFuelSurchargeClaimFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class MinimumSeasonalFuelSurchargeClaimAdjuster(var minimum: Double) : SeasonalFuelSurchargeClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelSeasonalFuelSurchargeClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalFuelSurchargeClaimFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalFuelSurchargeClaimAdjuster(var percent: Double) : SeasonalFuelSurchargeClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalFuelSurchargeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalFuelSurchargeClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalFuelSurchargeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFuelSurchargeClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFuelSurchargeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal fuel surcharge claim steps in order and keeps a trace of each. */
data class SeasonalFuelSurchargeClaimEngine(
    var steps: MutableList<SeasonalFuelSurchargeClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalFuelSurchargeClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalFuelSurchargeClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalFuelSurchargeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalFuelSurchargeClaimEngine(flags: Registry): SeasonalFuelSurchargeClaimEngine {
    return SeasonalFuelSurchargeClaimEngine(
        steps = mutableListOf<SeasonalFuelSurchargeClaimFactor>(
            SeasonalFuelSurchargeClaimSequenceOffsetAdjuster(1.16),
            MinimumSeasonalFuelSurchargeClaimAdjuster(16.2),
            LabelSeasonalFuelSurchargeClaimAdjuster(mutableMapOf<String, Double>("south" to 0.84)),
            PercentSeasonalFuelSurchargeClaimAdjuster(-8.5),
            TieredSeasonalFuelSurchargeClaimAdjuster(
                mutableListOf<Double>(3614.0, 9142.0, 9737.0),
                mutableListOf<Double>(0.88, 1.01, 1.02),
            ),
            FlaggedSeasonalFuelSurchargeClaimAdjuster(flags, "seasonal-fuel-surcharge-claim-weighted", 7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 14092.1,
    )
}
