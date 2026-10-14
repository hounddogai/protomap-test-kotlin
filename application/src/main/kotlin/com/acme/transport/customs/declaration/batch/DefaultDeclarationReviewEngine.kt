package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default declaration review calculation. */
interface DefaultDeclarationReviewStep {
    fun describe(): String
    fun adjust(item: DefaultDeclarationReview, value: Double): Double
}

data class RoundingDefaultDeclarationReviewAdjuster(var step: Double) : DefaultDeclarationReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDeclarationReview, value: Double): Double = roundTo(value, step)
}

data class CapDefaultDeclarationReviewAdjuster(var cap: Double) : DefaultDeclarationReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDeclarationReview, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultDeclarationReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultDeclarationReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultDeclarationReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultDeclarationReviewLimitOffsetAdjuster(var weight: Double) : DefaultDeclarationReviewStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: DefaultDeclarationReview, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class PercentDefaultDeclarationReviewAdjuster(var percent: Double) : DefaultDeclarationReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDeclarationReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default declaration review steps in order and keeps a trace of each. */
data class DefaultDeclarationReviewEngine(
    var steps: MutableList<DefaultDeclarationReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultDeclarationReview): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultDeclarationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultDeclarationReviewEngine(): DefaultDeclarationReviewEngine {
    return DefaultDeclarationReviewEngine(
        steps = mutableListOf<DefaultDeclarationReviewStep>(
            RoundingDefaultDeclarationReviewAdjuster(0.05),
            CapDefaultDeclarationReviewAdjuster(8159.0),
            TieredDefaultDeclarationReviewAdjuster(
                mutableListOf<Double>(1981.0, 7474.0, 8438.0),
                mutableListOf<Double>(0.95, 1.17, 1.16),
            ),
            DefaultDeclarationReviewLimitOffsetAdjuster(0.92),
            PercentDefaultDeclarationReviewAdjuster(-20.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 91186.1,
    )
}
