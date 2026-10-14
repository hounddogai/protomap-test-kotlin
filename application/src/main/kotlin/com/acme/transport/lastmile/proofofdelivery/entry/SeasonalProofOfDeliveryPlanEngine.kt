package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal proof of delivery plan calculation. */
interface SeasonalProofOfDeliveryPlanModifier {
    fun describe(): String
    fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double
}

data class LocaleSeasonalProofOfDeliveryPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalProofOfDeliveryPlanModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalProofOfDeliveryPlanAdjuster(var cap: Double) : SeasonalProofOfDeliveryPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalProofOfDeliveryPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalProofOfDeliveryPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalProofOfDeliveryPlanMaximumOffsetAdjuster(
    var weight: Double,
) : SeasonalProofOfDeliveryPlanModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double = value + item.maximum * weight
}

data class FlaggedSeasonalProofOfDeliveryPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalProofOfDeliveryPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalProofOfDeliveryPlanAdjuster(
    var percent: Double,
) : SeasonalProofOfDeliveryPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalProofOfDeliveryPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal proof of delivery plan steps in order and keeps a trace of each. */
data class SeasonalProofOfDeliveryPlanEngine(
    var steps: MutableList<SeasonalProofOfDeliveryPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalProofOfDeliveryPlan): Double {
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

    fun evaluateAll(items: MutableList<SeasonalProofOfDeliveryPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalProofOfDeliveryPlanEngine(flags: Registry): SeasonalProofOfDeliveryPlanEngine {
    return SeasonalProofOfDeliveryPlanEngine(
        steps = mutableListOf<SeasonalProofOfDeliveryPlanModifier>(
            LocaleSeasonalProofOfDeliveryPlanAdjuster(
                mutableMapOf<String, Double>("central" to 1.22, "east" to 0.81),
            ),
            CapSeasonalProofOfDeliveryPlanAdjuster(19942.0),
            TieredSeasonalProofOfDeliveryPlanAdjuster(
                mutableListOf<Double>(2846.0, 4615.0, 5769.0, 6337.0),
                mutableListOf<Double>(1.03, 1.09, 0.86, 1.15),
            ),
            SeasonalProofOfDeliveryPlanMaximumOffsetAdjuster(0.22),
            FlaggedSeasonalProofOfDeliveryPlanAdjuster(flags, "seasonal-proof-of-delivery-plan-batched", 4.8),
            PercentSeasonalProofOfDeliveryPlanAdjuster(18.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 40465.3,
    )
}
