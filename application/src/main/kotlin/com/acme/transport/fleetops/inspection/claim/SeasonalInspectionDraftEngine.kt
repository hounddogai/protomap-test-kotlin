package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal inspection draft calculation. */
interface SeasonalInspectionDraftAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalInspectionDraft, value: Double): Double
}

data class FlaggedSeasonalInspectionDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalInspectionDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalInspectionDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalInspectionDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalInspectionDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalInspectionDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalInspectionDraftLimitOffsetAdjuster(var weight: Double) : SeasonalInspectionDraftAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: SeasonalInspectionDraft, value: Double): Double = value + item.limit.toDouble() * weight
}

data class RoundingSeasonalInspectionDraftAdjuster(var step: Double) : SeasonalInspectionDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalInspectionDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal inspection draft steps in order and keeps a trace of each. */
data class SeasonalInspectionDraftEngine(
    var steps: MutableList<SeasonalInspectionDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalInspectionDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalInspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalInspectionDraftEngine(flags: Registry): SeasonalInspectionDraftEngine {
    return SeasonalInspectionDraftEngine(
        steps = mutableListOf<SeasonalInspectionDraftAdjuster>(
            FlaggedSeasonalInspectionDraftAdjuster(flags, "seasonal-inspection-draft-regional", 8.5),
            TieredSeasonalInspectionDraftAdjuster(
                mutableListOf<Double>(2502.0, 2742.0, 3989.0, 8278.0),
                mutableListOf<Double>(1.16, 0.95, 1.2, 1.02),
            ),
            SeasonalInspectionDraftLimitOffsetAdjuster(1.47),
            RoundingSeasonalInspectionDraftAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 46579.7,
    )
}
