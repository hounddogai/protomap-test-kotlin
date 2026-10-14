package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal parcel ledger calculation. */
interface SeasonalParcelLedgerModifier {
    fun describe(): String
    fun adjust(item: SeasonalParcelLedger, value: Double): Double
}

data class RoundingSeasonalParcelLedgerAdjuster(var step: Double) : SeasonalParcelLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalParcelLedger, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalParcelLedgerAdjuster(var minimum: Double) : SeasonalParcelLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalParcelLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalParcelLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalParcelLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalParcelLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal parcel ledger steps in order and keeps a trace of each. */
data class SeasonalParcelLedgerEngine(
    var steps: MutableList<SeasonalParcelLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalParcelLedger): Double {
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

    fun evaluateAll(items: MutableList<SeasonalParcelLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalParcelLedgerEngine(): SeasonalParcelLedgerEngine {
    return SeasonalParcelLedgerEngine(
        steps = mutableListOf<SeasonalParcelLedgerModifier>(
            RoundingSeasonalParcelLedgerAdjuster(0.1),
            MinimumSeasonalParcelLedgerAdjuster(45.9),
            TieredSeasonalParcelLedgerAdjuster(
                mutableListOf<Double>(355.0, 1404.0, 5952.0),
                mutableListOf<Double>(0.84, 1.07, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 90136.1,
    )
}
