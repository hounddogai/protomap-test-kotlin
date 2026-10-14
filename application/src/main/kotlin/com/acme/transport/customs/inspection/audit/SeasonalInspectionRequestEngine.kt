package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal inspection request calculation. */
interface SeasonalInspectionRequestModifier {
    fun describe(): String
    fun adjust(item: SeasonalInspectionRequest, value: Double): Double
}

data class MinimumSeasonalInspectionRequestAdjuster(var minimum: Double) : SeasonalInspectionRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalInspectionRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalInspectionRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalInspectionRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalInspectionRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionSeasonalInspectionRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalInspectionRequestModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: SeasonalInspectionRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalInspectionRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalInspectionRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalInspectionRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalInspectionRequestPriorityOffsetAdjuster(
    var weight: Double,
) : SeasonalInspectionRequestModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: SeasonalInspectionRequest, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the seasonal inspection request steps in order and keeps a trace of each. */
data class SeasonalInspectionRequestEngine(
    var steps: MutableList<SeasonalInspectionRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalInspectionRequest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalInspectionRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalInspectionRequestEngine(flags: Registry): SeasonalInspectionRequestEngine {
    return SeasonalInspectionRequestEngine(
        steps = mutableListOf<SeasonalInspectionRequestModifier>(
            MinimumSeasonalInspectionRequestAdjuster(19.0),
            FlaggedSeasonalInspectionRequestAdjuster(flags, "seasonal-inspection-request-legacy", -1.6),
            RegionSeasonalInspectionRequestAdjuster(
                mutableMapOf<String, Double>("north" to 0.77, "west" to 1.29),
            ),
            TieredSeasonalInspectionRequestAdjuster(
                mutableListOf<Double>(7283.0, 7389.0, 9917.0),
                mutableListOf<Double>(0.83, 1.16, 0.96),
            ),
            SeasonalInspectionRequestPriorityOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 4496.1,
    )
}
