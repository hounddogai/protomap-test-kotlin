package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal cutoff snapshot calculation. */
interface SeasonalCutoffSnapshotStep {
    fun describe(): String
    fun adjust(item: SeasonalCutoffSnapshot, value: Double): Double
}

data class LabelSeasonalCutoffSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCutoffSnapshotStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalCutoffSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSeasonalCutoffSnapshotAdjuster(var minimum: Double) : SeasonalCutoffSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCutoffSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalCutoffSnapshotRatioOffsetAdjuster(var weight: Double) : SeasonalCutoffSnapshotStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: SeasonalCutoffSnapshot, value: Double): Double = value + item.ratio * weight
}

data class RoundingSeasonalCutoffSnapshotAdjuster(var step: Double) : SeasonalCutoffSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCutoffSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal cutoff snapshot steps in order and keeps a trace of each. */
data class SeasonalCutoffSnapshotEngine(
    var steps: MutableList<SeasonalCutoffSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalCutoffSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCutoffSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCutoffSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalCutoffSnapshotEngine(): SeasonalCutoffSnapshotEngine {
    return SeasonalCutoffSnapshotEngine(
        steps = mutableListOf<SeasonalCutoffSnapshotStep>(
            LabelSeasonalCutoffSnapshotAdjuster(
                mutableMapOf<String, Double>("east" to 0.81, "central" to 1.26, "online" to 1.27),
            ),
            MinimumSeasonalCutoffSnapshotAdjuster(20.8),
            SeasonalCutoffSnapshotRatioOffsetAdjuster(0.46),
            RoundingSeasonalCutoffSnapshotAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 91853.9,
    )
}
