package com.acme.transport.customs.declaration.declarationmetric

/** One step of the lenient declaration metric calculation. */
interface LenientDeclarationMetricModifier {
    fun describe(): String
    fun adjust(item: LenientDeclarationMetric, value: Double): Double
}

data class CountryLenientDeclarationMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDeclarationMetricModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientDeclarationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientDeclarationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDeclarationMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDeclarationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLenientDeclarationMetricAdjuster(var cap: Double) : LenientDeclarationMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDeclarationMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient declaration metric steps in order and keeps a trace of each. */
data class LenientDeclarationMetricEngine(
    var steps: MutableList<LenientDeclarationMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDeclarationMetric): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientDeclarationMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientDeclarationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDeclarationMetricEngine(): LenientDeclarationMetricEngine {
    return LenientDeclarationMetricEngine(
        steps = mutableListOf<LenientDeclarationMetricModifier>(
            CountryLenientDeclarationMetricAdjuster(mutableMapOf<String, Double>("central" to 1.03)),
            TieredLenientDeclarationMetricAdjuster(
                mutableListOf<Double>(829.0, 4487.0),
                mutableListOf<Double>(0.85, 0.95),
            ),
            CapLenientDeclarationMetricAdjuster(30707.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 87380.0,
    )
}
