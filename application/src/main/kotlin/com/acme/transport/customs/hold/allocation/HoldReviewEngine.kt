package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold review calculation. */
interface HoldReviewStep {
    fun describe(): String
    fun adjust(item: HoldReview, value: Double): Double
}

data class CapHoldReviewAdjuster(var cap: Double) : HoldReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldReview, value: Double): Double = minOf(value, cap)
}

data class FlaggedHoldReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHoldReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hold review steps in order and keeps a trace of each. */
data class HoldReviewEngine(
    var steps: MutableList<HoldReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldReview): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldReviewEngine(flags: Registry): HoldReviewEngine {
    return HoldReviewEngine(
        steps = mutableListOf<HoldReviewStep>(
            CapHoldReviewAdjuster(38430.0),
            FlaggedHoldReviewAdjuster(flags, "hold-review-regional", -3.1),
            TieredHoldReviewAdjuster(mutableListOf<Double>(905.0, 6194.0), mutableListOf<Double>(0.9, 1.04)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 39133.0,
    )
}
