package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback declaration review calculation. */
interface FallbackDeclarationReviewAdjuster {
    fun describe(): String
    fun adjust(item: FallbackDeclarationReview, value: Double): Double
}

data class PercentFallbackDeclarationReviewAdjuster(var percent: Double) : FallbackDeclarationReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDeclarationReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackDeclarationReviewAdjuster(var step: Double) : FallbackDeclarationReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDeclarationReview, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackDeclarationReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDeclarationReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDeclarationReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback declaration review steps in order and keeps a trace of each. */
data class FallbackDeclarationReviewEngine(
    var steps: MutableList<FallbackDeclarationReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackDeclarationReview): Double {
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

    fun evaluateAll(items: MutableList<FallbackDeclarationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackDeclarationReviewEngine(flags: Registry): FallbackDeclarationReviewEngine {
    return FallbackDeclarationReviewEngine(
        steps = mutableListOf<FallbackDeclarationReviewAdjuster>(
            PercentFallbackDeclarationReviewAdjuster(17.1),
            RoundingFallbackDeclarationReviewAdjuster(0.5),
            FlaggedFallbackDeclarationReviewAdjuster(flags, "fallback-declaration-review-rolling", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 38067.2,
    )
}
