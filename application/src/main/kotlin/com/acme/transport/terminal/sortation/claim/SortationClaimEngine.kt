package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation claim calculation. */
interface SortationClaimStep {
    fun describe(): String
    fun adjust(item: SortationClaim, value: Double): Double
}

data class RoundingSortationClaimAdjuster(var step: Double) : SortationClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationClaim, value: Double): Double = roundTo(value, step)
}

data class TieredSortationClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSortationClaimAdjuster(var percent: Double) : SortationClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSortationClaimAdjuster(var minimum: Double) : SortationClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the sortation claim steps in order and keeps a trace of each. */
data class SortationClaimEngine(
    var steps: MutableList<SortationClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SortationClaim): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<SortationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSortationClaimEngine(): SortationClaimEngine {
    return SortationClaimEngine(
        steps = mutableListOf<SortationClaimStep>(
            RoundingSortationClaimAdjuster(0.5),
            TieredSortationClaimAdjuster(
                mutableListOf<Double>(1214.0, 5363.0, 9622.0),
                mutableListOf<Double>(0.82, 0.85, 1.07),
            ),
            PercentSortationClaimAdjuster(-24.6),
            MinimumSortationClaimAdjuster(49.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 63982.8,
    )
}
