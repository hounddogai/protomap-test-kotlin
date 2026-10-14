package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal proof of delivery milestone calculation. */
interface SeasonalProofOfDeliveryMilestoneFactor {
    fun describe(): String
    fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double
}

data class RoundingSeasonalProofOfDeliveryMilestoneAdjuster(
    var step: Double,
) : SeasonalProofOfDeliveryMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalProofOfDeliveryMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalProofOfDeliveryMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalProofOfDeliveryMilestoneAdjuster(
    var minimum: Double,
) : SeasonalProofOfDeliveryMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentSeasonalProofOfDeliveryMilestoneAdjuster(
    var percent: Double,
) : SeasonalProofOfDeliveryMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalProofOfDeliveryMilestoneRateOffsetAdjuster(
    var weight: Double,
) : SeasonalProofOfDeliveryMilestoneFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double = value + item.rate * weight
}

data class NameSeasonalProofOfDeliveryMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalProofOfDeliveryMilestoneFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalProofOfDeliveryMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal proof of delivery milestone steps in order and keeps a trace of each. */
data class SeasonalProofOfDeliveryMilestoneEngine(
    var steps: MutableList<SeasonalProofOfDeliveryMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalProofOfDeliveryMilestone): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalProofOfDeliveryMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalProofOfDeliveryMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalProofOfDeliveryMilestoneEngine(): SeasonalProofOfDeliveryMilestoneEngine {
    return SeasonalProofOfDeliveryMilestoneEngine(
        steps = mutableListOf<SeasonalProofOfDeliveryMilestoneFactor>(
            RoundingSeasonalProofOfDeliveryMilestoneAdjuster(0.1),
            TieredSeasonalProofOfDeliveryMilestoneAdjuster(
                mutableListOf<Double>(1434.0, 4796.0, 7242.0),
                mutableListOf<Double>(0.95, 1.02, 1.2),
            ),
            MinimumSeasonalProofOfDeliveryMilestoneAdjuster(42.4),
            PercentSeasonalProofOfDeliveryMilestoneAdjuster(19.9),
            SeasonalProofOfDeliveryMilestoneRateOffsetAdjuster(1.3),
            NameSeasonalProofOfDeliveryMilestoneAdjuster(
                mutableMapOf<String, Double>("north" to 1.06, "south" to 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 26446.4,
    )
}
