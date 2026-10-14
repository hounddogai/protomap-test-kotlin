package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal rest break ledger calculation. */
interface SeasonalRestBreakLedgerFactor {
    fun describe(): String
    fun adjust(item: SeasonalRestBreakLedger, value: Double): Double
}

data class TieredSeasonalRestBreakLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRestBreakLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRestBreakLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalRestBreakLedgerAdjuster(var cap: Double) : SeasonalRestBreakLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRestBreakLedger, value: Double): Double = minOf(value, cap)
}

data class SeasonalRestBreakLedgerMinimumOffsetAdjuster(var weight: Double) : SeasonalRestBreakLedgerFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: SeasonalRestBreakLedger, value: Double): Double = value + item.minimum * weight
}

data class RoundingSeasonalRestBreakLedgerAdjuster(var step: Double) : SeasonalRestBreakLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRestBreakLedger, value: Double): Double = roundTo(value, step)
}

data class TitleSeasonalRestBreakLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRestBreakLedgerFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalRestBreakLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal rest break ledger steps in order and keeps a trace of each. */
data class SeasonalRestBreakLedgerEngine(
    var steps: MutableList<SeasonalRestBreakLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalRestBreakLedger): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalRestBreakLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalRestBreakLedgerEngine(): SeasonalRestBreakLedgerEngine {
    return SeasonalRestBreakLedgerEngine(
        steps = mutableListOf<SeasonalRestBreakLedgerFactor>(
            TieredSeasonalRestBreakLedgerAdjuster(
                mutableListOf<Double>(4338.0, 7360.0, 8634.0),
                mutableListOf<Double>(0.82, 0.93, 1.07),
            ),
            CapSeasonalRestBreakLedgerAdjuster(27453.0),
            SeasonalRestBreakLedgerMinimumOffsetAdjuster(1.08),
            RoundingSeasonalRestBreakLedgerAdjuster(0.5),
            TitleSeasonalRestBreakLedgerAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 77834.0,
    )
}
