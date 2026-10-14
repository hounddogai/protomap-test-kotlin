package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient carbon report manifest calculation. */
interface LenientCarbonReportManifestModifier {
    fun describe(): String
    fun adjust(item: LenientCarbonReportManifest, value: Double): Double
}

data class LenientCarbonReportManifestMarginOffsetAdjuster(
    var weight: Double,
) : LenientCarbonReportManifestModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LenientCarbonReportManifest, value: Double): Double = value + item.margin * weight
}

data class PercentLenientCarbonReportManifestAdjuster(
    var percent: Double,
) : LenientCarbonReportManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCarbonReportManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientCarbonReportManifestAdjuster(var step: Double) : LenientCarbonReportManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCarbonReportManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient carbon report manifest steps in order and keeps a trace of each. */
data class LenientCarbonReportManifestEngine(
    var steps: MutableList<LenientCarbonReportManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientCarbonReportManifest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCarbonReportManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientCarbonReportManifestEngine(): LenientCarbonReportManifestEngine {
    return LenientCarbonReportManifestEngine(
        steps = mutableListOf<LenientCarbonReportManifestModifier>(
            LenientCarbonReportManifestMarginOffsetAdjuster(1.26),
            PercentLenientCarbonReportManifestAdjuster(5.8),
            RoundingLenientCarbonReportManifestAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 94870.3,
    )
}
