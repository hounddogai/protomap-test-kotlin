package com.acme.transport.customs.declaration.declarationsnapshot

/** One step of the seasonal declaration assignment calculation. */
interface SeasonalDeclarationAssignmentStep {
    fun describe(): String
    fun adjust(item: SeasonalDeclarationAssignment, value: Double): Double
}

data class TieredSeasonalDeclarationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDeclarationAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDeclarationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalDeclarationAssignmentBatchSizeOffsetAdjuster(
    var weight: Double,
) : SeasonalDeclarationAssignmentStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: SeasonalDeclarationAssignment, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CapSeasonalDeclarationAssignmentAdjuster(var cap: Double) : SeasonalDeclarationAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDeclarationAssignment, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalDeclarationAssignmentAdjuster(
    var minimum: Double,
) : SeasonalDeclarationAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDeclarationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentSeasonalDeclarationAssignmentAdjuster(
    var percent: Double,
) : SeasonalDeclarationAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDeclarationAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal declaration assignment steps in order and keeps a trace of each. */
data class SeasonalDeclarationAssignmentEngine(
    var steps: MutableList<SeasonalDeclarationAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalDeclarationAssignment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDeclarationAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDeclarationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalDeclarationAssignmentEngine(): SeasonalDeclarationAssignmentEngine {
    return SeasonalDeclarationAssignmentEngine(
        steps = mutableListOf<SeasonalDeclarationAssignmentStep>(
            TieredSeasonalDeclarationAssignmentAdjuster(
                mutableListOf<Double>(7840.0, 9011.0),
                mutableListOf<Double>(1.02, 1.04),
            ),
            SeasonalDeclarationAssignmentBatchSizeOffsetAdjuster(0.67),
            CapSeasonalDeclarationAssignmentAdjuster(8717.0),
            MinimumSeasonalDeclarationAssignmentAdjuster(25.7),
            PercentSeasonalDeclarationAssignmentAdjuster(-20.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 83306.4,
    )
}
