package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal qualification mapping calculation. */
interface SeasonalQualificationMappingFactor {
    fun describe(): String
    fun adjust(item: SeasonalQualificationMapping, value: Double): Double
}

data class RoundingSeasonalQualificationMappingAdjuster(var step: Double) : SeasonalQualificationMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalQualificationMapping, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalQualificationMappingAdjuster(var cap: Double) : SeasonalQualificationMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalQualificationMapping, value: Double): Double = minOf(value, cap)
}

data class SourceSeasonalQualificationMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalQualificationMappingFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: SeasonalQualificationMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal qualification mapping steps in order and keeps a trace of each. */
data class SeasonalQualificationMappingEngine(
    var steps: MutableList<SeasonalQualificationMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalQualificationMapping): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalQualificationMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalQualificationMappingEngine(): SeasonalQualificationMappingEngine {
    return SeasonalQualificationMappingEngine(
        steps = mutableListOf<SeasonalQualificationMappingFactor>(
            RoundingSeasonalQualificationMappingAdjuster(0.05),
            CapSeasonalQualificationMappingAdjuster(21197.0),
            SourceSeasonalQualificationMappingAdjuster(
                mutableMapOf<String, Double>("south" to 1.03, "central" to 1.19, "online" to 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 81015.7,
    )
}
