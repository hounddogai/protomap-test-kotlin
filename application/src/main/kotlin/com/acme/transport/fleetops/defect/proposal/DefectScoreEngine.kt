package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the defect score calculation. */
interface DefectScoreModifier {
    fun describe(): String
    fun adjust(item: DefectScore, value: Double): Double
}

data class RoundingDefectScoreAdjuster(var step: Double) : DefectScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectScore, value: Double): Double = roundTo(value, step)
}

data class DefectScorePriorityOffsetAdjuster(var weight: Double) : DefectScoreModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: DefectScore, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CapDefectScoreAdjuster(var cap: Double) : DefectScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectScore, value: Double): Double = minOf(value, cap)
}

data class FlaggedDefectScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelDefectScoreAdjuster(var factors: MutableMap<String, Double>) : DefectScoreModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: DefectScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefectScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the defect score steps in order and keeps a trace of each. */
data class DefectScoreEngine(
    var steps: MutableList<DefectScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefectScore): Double {
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

    fun evaluateAll(items: MutableList<DefectScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefectScoreEngine(flags: Registry): DefectScoreEngine {
    return DefectScoreEngine(
        steps = mutableListOf<DefectScoreModifier>(
            RoundingDefectScoreAdjuster(5.0),
            DefectScorePriorityOffsetAdjuster(0.55),
            CapDefectScoreAdjuster(30916.0),
            FlaggedDefectScoreAdjuster(flags, "defect-score-scheduled", 7.7),
            LabelDefectScoreAdjuster(
                mutableMapOf<String, Double>("online" to 0.99, "retail" to 1.1, "south" to 1.38),
            ),
            TieredDefectScoreAdjuster(
                mutableListOf<Double>(851.0, 1935.0, 5658.0, 6548.0),
                mutableListOf<Double>(1.07, 0.87, 1.09, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 11807.2,
    )
}
