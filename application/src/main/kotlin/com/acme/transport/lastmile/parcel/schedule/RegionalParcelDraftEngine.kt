package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional parcel draft calculation. */
interface RegionalParcelDraftModifier {
    fun describe(): String
    fun adjust(item: RegionalParcelDraft, value: Double): Double
}

data class RegionalParcelDraftRatioOffsetAdjuster(var weight: Double) : RegionalParcelDraftModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RegionalParcelDraft, value: Double): Double = value + item.ratio * weight
}

data class MinimumRegionalParcelDraftAdjuster(var minimum: Double) : RegionalParcelDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalParcelDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalParcelDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalParcelDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalParcelDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalParcelDraftAdjuster(var cap: Double) : RegionalParcelDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalParcelDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the regional parcel draft steps in order and keeps a trace of each. */
data class RegionalParcelDraftEngine(
    var steps: MutableList<RegionalParcelDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalParcelDraft): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalParcelDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalParcelDraftEngine(): RegionalParcelDraftEngine {
    return RegionalParcelDraftEngine(
        steps = mutableListOf<RegionalParcelDraftModifier>(
            RegionalParcelDraftRatioOffsetAdjuster(0.1),
            MinimumRegionalParcelDraftAdjuster(33.3),
            TieredRegionalParcelDraftAdjuster(
                mutableListOf<Double>(64.0, 326.0, 4424.0),
                mutableListOf<Double>(1.18, 1.08, 0.91),
            ),
            CapRegionalParcelDraftAdjuster(14133.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 39667.4,
    )
}
