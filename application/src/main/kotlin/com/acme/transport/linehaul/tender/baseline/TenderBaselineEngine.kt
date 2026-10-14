package com.acme.transport.linehaul.tender.baseline

/** One step of the tender baseline calculation. */
interface TenderBaselineAdjuster {
    fun describe(): String
    fun adjust(item: TenderBaseline, value: Double): Double
}

data class PercentTenderBaselineAdjuster(var percent: Double) : TenderBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTenderBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelTenderBaselineAdjuster(var factors: MutableMap<String, Double>) : TenderBaselineAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TenderBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TenderBaselineScoreOffsetAdjuster(var weight: Double) : TenderBaselineAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TenderBaseline, value: Double): Double = value + item.score * weight
}

data class CapTenderBaselineAdjuster(var cap: Double) : TenderBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the tender baseline steps in order and keeps a trace of each. */
data class TenderBaselineEngine(
    var steps: MutableList<TenderBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderBaseline): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TenderBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderBaselineEngine(): TenderBaselineEngine {
    return TenderBaselineEngine(
        steps = mutableListOf<TenderBaselineAdjuster>(
            PercentTenderBaselineAdjuster(-24.5),
            TieredTenderBaselineAdjuster(
                mutableListOf<Double>(6389.0, 7691.0),
                mutableListOf<Double>(1.19, 1.08),
            ),
            LabelTenderBaselineAdjuster(mutableMapOf<String, Double>("central" to 0.95, "south" to 0.74)),
            TenderBaselineScoreOffsetAdjuster(0.78),
            CapTenderBaselineAdjuster(48640.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 68845.3,
    )
}
