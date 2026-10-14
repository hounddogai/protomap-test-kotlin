package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tender estimate calculation. */
interface ManualTenderEstimateModifier {
    fun describe(): String
    fun adjust(item: ManualTenderEstimate, value: Double): Double
}

data class LocaleManualTenderEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTenderEstimateModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualTenderEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualTenderEstimateAdjuster(var step: Double) : ManualTenderEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTenderEstimate, value: Double): Double = roundTo(value, step)
}

data class MinimumManualTenderEstimateAdjuster(var minimum: Double) : ManualTenderEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTenderEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualTenderEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTenderEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTenderEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualTenderEstimateAdjuster(var percent: Double) : ManualTenderEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTenderEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual tender estimate steps in order and keeps a trace of each. */
data class ManualTenderEstimateEngine(
    var steps: MutableList<ManualTenderEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTenderEstimate): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTenderEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTenderEstimateEngine(): ManualTenderEstimateEngine {
    return ManualTenderEstimateEngine(
        steps = mutableListOf<ManualTenderEstimateModifier>(
            LocaleManualTenderEstimateAdjuster(mutableMapOf<String, Double>("central" to 1.23)),
            RoundingManualTenderEstimateAdjuster(0.05),
            MinimumManualTenderEstimateAdjuster(38.7),
            TieredManualTenderEstimateAdjuster(
                mutableListOf<Double>(4841.0, 5081.0, 5698.0, 8256.0),
                mutableListOf<Double>(0.88, 1.11, 1.16, 0.83),
            ),
            PercentManualTenderEstimateAdjuster(-9.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 70112.6,
    )
}
