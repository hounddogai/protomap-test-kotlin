package com.acme.transport.terminal.dwell.dwellcycle

/** One step of the lenient dwell review calculation. */
interface LenientDwellReviewModifier {
    fun describe(): String
    fun adjust(item: LenientDwellReview, value: Double): Double
}

data class LenientDwellReviewPalletsOffsetAdjuster(var weight: Double) : LenientDwellReviewModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: LenientDwellReview, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TieredLenientDwellReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDwellReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDwellReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotLenientDwellReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDwellReviewModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: LenientDwellReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient dwell review steps in order and keeps a trace of each. */
data class LenientDwellReviewEngine(
    var steps: MutableList<LenientDwellReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDwellReview): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientDwellReviewModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDwellReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDwellReviewEngine(): LenientDwellReviewEngine {
    return LenientDwellReviewEngine(
        steps = mutableListOf<LenientDwellReviewModifier>(
            LenientDwellReviewPalletsOffsetAdjuster(0.66),
            TieredLenientDwellReviewAdjuster(
                mutableListOf<Double>(3571.0, 5106.0, 6200.0),
                mutableListOf<Double>(0.81, 1.05, 1.15),
            ),
            DepotLenientDwellReviewAdjuster(mutableMapOf<String, Double>("west" to 0.78)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 64556.3,
    )
}
