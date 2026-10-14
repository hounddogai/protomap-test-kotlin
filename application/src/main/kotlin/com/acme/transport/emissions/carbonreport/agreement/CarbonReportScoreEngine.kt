package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the carbon report score calculation. */
interface CarbonReportScoreModifier {
    fun describe(): String
    fun adjust(item: CarbonReportScore, value: Double): Double
}

data class RoundingCarbonReportScoreAdjuster(var step: Double) : CarbonReportScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedCarbonReportScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleCarbonReportScoreAdjuster(var factors: MutableMap<String, Double>) : CarbonReportScoreModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CarbonReportScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCarbonReportScoreAdjuster(var minimum: Double) : CarbonReportScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCarbonReportScoreAdjuster(var percent: Double) : CarbonReportScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report score steps in order and keeps a trace of each. */
data class CarbonReportScoreEngine(
    var steps: MutableList<CarbonReportScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CarbonReportScore): Double {
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

    fun evaluateAll(items: MutableList<CarbonReportScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCarbonReportScoreEngine(flags: Registry): CarbonReportScoreEngine {
    return CarbonReportScoreEngine(
        steps = mutableListOf<CarbonReportScoreModifier>(
            RoundingCarbonReportScoreAdjuster(0.25),
            FlaggedCarbonReportScoreAdjuster(flags, "carbon-report-score-batched", -4.9),
            TitleCarbonReportScoreAdjuster(mutableMapOf<String, Double>("north" to 1.1, "south" to 0.73)),
            MinimumCarbonReportScoreAdjuster(9.5),
            PercentCarbonReportScoreAdjuster(-24.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 46039.2,
    )
}
