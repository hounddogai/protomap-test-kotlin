package com.acme.transport.freightpay.accessorial.request

/** One step of the weighted accessorial template calculation. */
interface WeightedAccessorialTemplateStep {
    fun describe(): String
    fun adjust(item: WeightedAccessorialTemplate, value: Double): Double
}

data class TieredWeightedAccessorialTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedAccessorialTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedAccessorialTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedAccessorialTemplateAdjuster(var cap: Double) : WeightedAccessorialTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAccessorialTemplate, value: Double): Double = minOf(value, cap)
}

data class SegmentWeightedAccessorialTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAccessorialTemplateStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: WeightedAccessorialTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedAccessorialTemplateAdjuster(var percent: Double) : WeightedAccessorialTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAccessorialTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted accessorial template steps in order and keeps a trace of each. */
data class WeightedAccessorialTemplateEngine(
    var steps: MutableList<WeightedAccessorialTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedAccessorialTemplate): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAccessorialTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAccessorialTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedAccessorialTemplateEngine(): WeightedAccessorialTemplateEngine {
    return WeightedAccessorialTemplateEngine(
        steps = mutableListOf<WeightedAccessorialTemplateStep>(
            TieredWeightedAccessorialTemplateAdjuster(
                mutableListOf<Double>(1098.0, 5627.0, 6996.0),
                mutableListOf<Double>(0.89, 1.08, 1.08),
            ),
            CapWeightedAccessorialTemplateAdjuster(6433.0),
            SegmentWeightedAccessorialTemplateAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            PercentWeightedAccessorialTemplateAdjuster(2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 22587.4,
    )
}
