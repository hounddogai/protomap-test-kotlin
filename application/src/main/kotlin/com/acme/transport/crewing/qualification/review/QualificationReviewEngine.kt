package com.acme.transport.crewing.qualification.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification review calculation. */
interface QualificationReviewAdjuster {
    fun describe(): String
    fun adjust(item: QualificationReview, value: Double): Double
}

data class CapQualificationReviewAdjuster(var cap: Double) : QualificationReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationReview, value: Double): Double = minOf(value, cap)
}

data class RoundingQualificationReviewAdjuster(var step: Double) : QualificationReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationReview, value: Double): Double = roundTo(value, step)
}

data class PercentQualificationReviewAdjuster(var percent: Double) : QualificationReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification review steps in order and keeps a trace of each. */
data class QualificationReviewEngine(
    var steps: MutableList<QualificationReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationReview): Double {
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

    fun evaluateAll(items: MutableList<QualificationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationReviewEngine(): QualificationReviewEngine {
    return QualificationReviewEngine(
        steps = mutableListOf<QualificationReviewAdjuster>(
            CapQualificationReviewAdjuster(23910.0),
            RoundingQualificationReviewAdjuster(0.05),
            PercentQualificationReviewAdjuster(-17.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 60619.6,
    )
}
