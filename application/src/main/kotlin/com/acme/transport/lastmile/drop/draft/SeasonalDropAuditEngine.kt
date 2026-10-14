package com.acme.transport.lastmile.drop.draft

/** One step of the seasonal drop audit calculation. */
interface SeasonalDropAuditModifier {
    fun describe(): String
    fun adjust(item: SeasonalDropAudit, value: Double): Double
}

data class CapSeasonalDropAuditAdjuster(var cap: Double) : SeasonalDropAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDropAudit, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalDropAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDropAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDropAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalDropAuditScoreOffsetAdjuster(var weight: Double) : SeasonalDropAuditModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: SeasonalDropAudit, value: Double): Double = value + item.score * weight
}

/** Runs the seasonal drop audit steps in order and keeps a trace of each. */
data class SeasonalDropAuditEngine(
    var steps: MutableList<SeasonalDropAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDropAudit): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDropAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDropAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDropAuditEngine(): SeasonalDropAuditEngine {
    return SeasonalDropAuditEngine(
        steps = mutableListOf<SeasonalDropAuditModifier>(
            CapSeasonalDropAuditAdjuster(15108.0),
            TieredSeasonalDropAuditAdjuster(
                mutableListOf<Double>(4710.0, 4870.0),
                mutableListOf<Double>(1.19, 0.99),
            ),
            SeasonalDropAuditScoreOffsetAdjuster(0.76),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 25289.9,
    )
}
