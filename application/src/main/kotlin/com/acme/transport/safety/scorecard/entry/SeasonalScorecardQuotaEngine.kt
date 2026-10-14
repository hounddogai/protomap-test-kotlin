package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal scorecard quota calculation. */
interface SeasonalScorecardQuotaModifier {
    fun describe(): String
    fun adjust(item: SeasonalScorecardQuota, value: Double): Double
}

data class TieredSeasonalScorecardQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalScorecardQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalScorecardQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalScorecardQuotaAdjuster(var cap: Double) : SeasonalScorecardQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalScorecardQuota, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalScorecardQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalScorecardQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalScorecardQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal scorecard quota steps in order and keeps a trace of each. */
data class SeasonalScorecardQuotaEngine(
    var steps: MutableList<SeasonalScorecardQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalScorecardQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalScorecardQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalScorecardQuotaEngine(flags: Registry): SeasonalScorecardQuotaEngine {
    return SeasonalScorecardQuotaEngine(
        steps = mutableListOf<SeasonalScorecardQuotaModifier>(
            TieredSeasonalScorecardQuotaAdjuster(
                mutableListOf<Double>(1049.0, 3134.0, 6024.0),
                mutableListOf<Double>(0.85, 1.08, 0.89),
            ),
            CapSeasonalScorecardQuotaAdjuster(48673.0),
            FlaggedSeasonalScorecardQuotaAdjuster(flags, "seasonal-scorecard-quota-fallback", 3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 76928.2,
    )
}
