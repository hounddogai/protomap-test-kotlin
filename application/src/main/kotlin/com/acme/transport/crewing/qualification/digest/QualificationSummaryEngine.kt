package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the qualification summary calculation. */
interface QualificationSummaryFactor {
    fun describe(): String
    fun adjust(item: QualificationSummary, value: Double): Double
}

data class QualificationSummaryEndsAtOffsetAdjuster(var weight: Double) : QualificationSummaryFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: QualificationSummary, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentQualificationSummaryAdjuster(var percent: Double) : QualificationSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapQualificationSummaryAdjuster(var cap: Double) : QualificationSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the qualification summary steps in order and keeps a trace of each. */
data class QualificationSummaryEngine(
    var steps: MutableList<QualificationSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationSummary): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationSummaryEngine(): QualificationSummaryEngine {
    return QualificationSummaryEngine(
        steps = mutableListOf<QualificationSummaryFactor>(
            QualificationSummaryEndsAtOffsetAdjuster(1.3),
            PercentQualificationSummaryAdjuster(-1.4),
            CapQualificationSummaryAdjuster(26984.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 97857.2,
    )
}
