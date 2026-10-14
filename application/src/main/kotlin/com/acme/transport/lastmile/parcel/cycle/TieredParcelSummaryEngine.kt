package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered parcel summary calculation. */
interface TieredParcelSummaryAdjuster {
    fun describe(): String
    fun adjust(item: TieredParcelSummary, value: Double): Double
}

data class CapTieredParcelSummaryAdjuster(var cap: Double) : TieredParcelSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredParcelSummary, value: Double): Double = minOf(value, cap)
}

data class TieredParcelSummaryUpdatedAtOffsetAdjuster(var weight: Double) : TieredParcelSummaryAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TieredParcelSummary, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class TieredTieredParcelSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredParcelSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredParcelSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredParcelSummaryAdjuster(var minimum: Double) : TieredParcelSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredParcelSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredParcelSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredParcelSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredParcelSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTieredParcelSummaryAdjuster(var step: Double) : TieredParcelSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredParcelSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered parcel summary steps in order and keeps a trace of each. */
data class TieredParcelSummaryEngine(
    var steps: MutableList<TieredParcelSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredParcelSummary): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredParcelSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredParcelSummaryEngine(flags: Registry): TieredParcelSummaryEngine {
    return TieredParcelSummaryEngine(
        steps = mutableListOf<TieredParcelSummaryAdjuster>(
            CapTieredParcelSummaryAdjuster(12260.0),
            TieredParcelSummaryUpdatedAtOffsetAdjuster(0.76),
            TieredTieredParcelSummaryAdjuster(
                mutableListOf<Double>(26.0, 2557.0, 6874.0, 9231.0),
                mutableListOf<Double>(1.06, 0.9, 0.96, 0.99),
            ),
            MinimumTieredParcelSummaryAdjuster(14.9),
            FlaggedTieredParcelSummaryAdjuster(flags, "tiered-parcel-summary-adaptive", -6.9),
            RoundingTieredParcelSummaryAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 88288.9,
    )
}
