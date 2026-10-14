package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped qualification summary calculation. */
interface CappedQualificationSummaryStep {
    fun describe(): String
    fun adjust(item: CappedQualificationSummary, value: Double): Double
}

data class CapCappedQualificationSummaryAdjuster(var cap: Double) : CappedQualificationSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedQualificationSummary, value: Double): Double = minOf(value, cap)
}

data class CappedQualificationSummaryCreatedAtOffsetAdjuster(
    var weight: Double,
) : CappedQualificationSummaryStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: CappedQualificationSummary, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class MinimumCappedQualificationSummaryAdjuster(var minimum: Double) : CappedQualificationSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedQualificationSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedQualificationSummaryAdjuster(var percent: Double) : CappedQualificationSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedQualificationSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped qualification summary steps in order and keeps a trace of each. */
data class CappedQualificationSummaryEngine(
    var steps: MutableList<CappedQualificationSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedQualificationSummary): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedQualificationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedQualificationSummaryEngine(): CappedQualificationSummaryEngine {
    return CappedQualificationSummaryEngine(
        steps = mutableListOf<CappedQualificationSummaryStep>(
            CapCappedQualificationSummaryAdjuster(44383.0),
            CappedQualificationSummaryCreatedAtOffsetAdjuster(1.94),
            MinimumCappedQualificationSummaryAdjuster(36.4),
            PercentCappedQualificationSummaryAdjuster(11.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 64130.1,
    )
}
