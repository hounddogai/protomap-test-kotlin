package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict carbon report exception calculation. */
interface StrictCarbonReportExceptionFactor {
    fun describe(): String
    fun adjust(item: StrictCarbonReportException, value: Double): Double
}

data class CapStrictCarbonReportExceptionAdjuster(var cap: Double) : StrictCarbonReportExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCarbonReportException, value: Double): Double = minOf(value, cap)
}

data class TieredStrictCarbonReportExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCarbonReportExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCarbonReportException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelStrictCarbonReportExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictCarbonReportExceptionFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictCarbonReportException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictCarbonReportExceptionRevisionOffsetAdjuster(
    var weight: Double,
) : StrictCarbonReportExceptionFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: StrictCarbonReportException, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class MinimumStrictCarbonReportExceptionAdjuster(var minimum: Double) : StrictCarbonReportExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCarbonReportException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictCarbonReportExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCarbonReportExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCarbonReportException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict carbon report exception steps in order and keeps a trace of each. */
data class StrictCarbonReportExceptionEngine(
    var steps: MutableList<StrictCarbonReportExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictCarbonReportException): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictCarbonReportException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictCarbonReportExceptionEngine(flags: Registry): StrictCarbonReportExceptionEngine {
    return StrictCarbonReportExceptionEngine(
        steps = mutableListOf<StrictCarbonReportExceptionFactor>(
            CapStrictCarbonReportExceptionAdjuster(32241.0),
            TieredStrictCarbonReportExceptionAdjuster(
                mutableListOf<Double>(7350.0, 8126.0, 8509.0),
                mutableListOf<Double>(1.03, 1.12, 1.07),
            ),
            LabelStrictCarbonReportExceptionAdjuster(mutableMapOf<String, Double>("south" to 0.79)),
            StrictCarbonReportExceptionRevisionOffsetAdjuster(0.91),
            MinimumStrictCarbonReportExceptionAdjuster(43.5),
            FlaggedStrictCarbonReportExceptionAdjuster(flags, "strict-carbon-report-exception-tiered", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 84100.1,
    )
}
