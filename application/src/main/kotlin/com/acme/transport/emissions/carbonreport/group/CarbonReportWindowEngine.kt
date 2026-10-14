package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report window calculation. */
interface CarbonReportWindowFactor {
    fun describe(): String
    fun adjust(item: CarbonReportWindow, value: Double): Double
}

data class CapCarbonReportWindowAdjuster(var cap: Double) : CarbonReportWindowFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportWindow, value: Double): Double = minOf(value, cap)
}

data class ChannelCarbonReportWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportWindowFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: CarbonReportWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCarbonReportWindowAdjuster(var percent: Double) : CarbonReportWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report window steps in order and keeps a trace of each. */
data class CarbonReportWindowEngine(
    var steps: MutableList<CarbonReportWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportWindow): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportWindowEngine(): CarbonReportWindowEngine {
    return CarbonReportWindowEngine(
        steps = mutableListOf<CarbonReportWindowFactor>(
            CapCarbonReportWindowAdjuster(27068.0),
            ChannelCarbonReportWindowAdjuster(mutableMapOf<String, Double>("retail" to 1.17)),
            PercentCarbonReportWindowAdjuster(2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 91327.6,
    )
}
