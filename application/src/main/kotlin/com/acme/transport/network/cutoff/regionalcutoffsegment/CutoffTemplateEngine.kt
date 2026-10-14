package com.acme.transport.network.cutoff.regionalcutoffsegment

/** One step of the cutoff template calculation. */
interface CutoffTemplateModifier {
    fun describe(): String
    fun adjust(item: CutoffTemplate, value: Double): Double
}

data class PercentCutoffTemplateAdjuster(var percent: Double) : CutoffTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryCutoffTemplateAdjuster(var factors: MutableMap<String, Double>) : CutoffTemplateModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: CutoffTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCutoffTemplateAdjuster(var cap: Double) : CutoffTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumCutoffTemplateAdjuster(var minimum: Double) : CutoffTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCutoffTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cutoff template steps in order and keeps a trace of each. */
data class CutoffTemplateEngine(
    var steps: MutableList<CutoffTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CutoffTemplate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCutoffTemplateEngine(): CutoffTemplateEngine {
    return CutoffTemplateEngine(
        steps = mutableListOf<CutoffTemplateModifier>(
            PercentCutoffTemplateAdjuster(10.1),
            CountryCutoffTemplateAdjuster(
                mutableMapOf<String, Double>("west" to 1.33, "central" to 0.9, "north" to 1.34),
            ),
            CapCutoffTemplateAdjuster(15636.0),
            MinimumCutoffTemplateAdjuster(44.6),
            TieredCutoffTemplateAdjuster(
                mutableListOf<Double>(2224.0, 6151.0, 6159.0, 8369.0),
                mutableListOf<Double>(1.06, 0.86, 1.06, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 71867.1,
    )
}
