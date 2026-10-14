package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal redelivery audit calculation. */
interface SeasonalRedeliveryAuditFactor {
    fun describe(): String
    fun adjust(item: SeasonalRedeliveryAudit, value: Double): Double
}

data class RoundingSeasonalRedeliveryAuditAdjuster(var step: Double) : SeasonalRedeliveryAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRedeliveryAudit, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalRedeliveryAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRedeliveryAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRedeliveryAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalRedeliveryAuditAdjuster(var cap: Double) : SeasonalRedeliveryAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRedeliveryAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal redelivery audit steps in order and keeps a trace of each. */
data class SeasonalRedeliveryAuditEngine(
    var steps: MutableList<SeasonalRedeliveryAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalRedeliveryAudit): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalRedeliveryAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalRedeliveryAuditEngine(): SeasonalRedeliveryAuditEngine {
    return SeasonalRedeliveryAuditEngine(
        steps = mutableListOf<SeasonalRedeliveryAuditFactor>(
            RoundingSeasonalRedeliveryAuditAdjuster(0.1),
            TieredSeasonalRedeliveryAuditAdjuster(
                mutableListOf<Double>(3264.0, 6045.0, 8746.0),
                mutableListOf<Double>(1.06, 0.99, 1.16),
            ),
            CapSeasonalRedeliveryAuditAdjuster(29858.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 14794.2,
    )
}
