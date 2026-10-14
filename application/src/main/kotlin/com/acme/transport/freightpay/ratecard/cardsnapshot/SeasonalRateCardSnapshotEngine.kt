package com.acme.transport.freightpay.ratecard.cardsnapshot

/** One step of the seasonal rate card snapshot calculation. */
interface SeasonalRateCardSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalRateCardSnapshot, value: Double): Double
}

data class MinimumSeasonalRateCardSnapshotAdjuster(var minimum: Double) : SeasonalRateCardSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRateCardSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalRateCardSnapshotAdjuster(var cap: Double) : SeasonalRateCardSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRateCardSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalRateCardSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRateCardSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRateCardSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal rate card snapshot steps in order and keeps a trace of each. */
data class SeasonalRateCardSnapshotEngine(
    var steps: MutableList<SeasonalRateCardSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalRateCardSnapshot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRateCardSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRateCardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalRateCardSnapshotEngine(): SeasonalRateCardSnapshotEngine {
    return SeasonalRateCardSnapshotEngine(
        steps = mutableListOf<SeasonalRateCardSnapshotAdjuster>(
            MinimumSeasonalRateCardSnapshotAdjuster(1.8),
            CapSeasonalRateCardSnapshotAdjuster(14604.0),
            TieredSeasonalRateCardSnapshotAdjuster(
                mutableListOf<Double>(5100.0, 7176.0),
                mutableListOf<Double>(1.04, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 66412.3,
    )
}
