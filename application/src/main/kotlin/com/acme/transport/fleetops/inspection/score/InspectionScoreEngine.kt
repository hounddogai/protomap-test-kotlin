package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection score calculation. */
interface InspectionScoreFactor {
    fun describe(): String
    fun adjust(item: InspectionScore, value: Double): Double
}

data class TieredInspectionScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentInspectionScoreAdjuster(var percent: Double) : InspectionScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedInspectionScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelInspectionScoreAdjuster(var factors: MutableMap<String, Double>) : InspectionScoreFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection score steps in order and keeps a trace of each. */
data class InspectionScoreEngine(
    var steps: MutableList<InspectionScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionScore): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionScoreEngine(flags: Registry): InspectionScoreEngine {
    return InspectionScoreEngine(
        steps = mutableListOf<InspectionScoreFactor>(
            TieredInspectionScoreAdjuster(
                mutableListOf<Double>(1724.0, 2956.0, 5602.0, 7260.0),
                mutableListOf<Double>(0.83, 1.02, 1.08, 1.05),
            ),
            PercentInspectionScoreAdjuster(-14.5),
            FlaggedInspectionScoreAdjuster(flags, "inspection-score-manual", -9.4),
            LabelInspectionScoreAdjuster(mutableMapOf<String, Double>("south" to 0.75)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 25956.5,
    )
}
