package com.acme.transport.terminal.dwell.digest

/** One step of the fallback dwell score calculation. */
interface FallbackDwellScoreAdjuster {
    fun describe(): String
    fun adjust(item: FallbackDwellScore, value: Double): Double
}

data class PercentFallbackDwellScoreAdjuster(var percent: Double) : FallbackDwellScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDwellScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackDwellScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDwellScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDwellScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotFallbackDwellScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDwellScoreAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackDwellScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback dwell score steps in order and keeps a trace of each. */
data class FallbackDwellScoreEngine(
    var steps: MutableList<FallbackDwellScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDwellScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDwellScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDwellScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDwellScoreEngine(): FallbackDwellScoreEngine {
    return FallbackDwellScoreEngine(
        steps = mutableListOf<FallbackDwellScoreAdjuster>(
            PercentFallbackDwellScoreAdjuster(4.8),
            TieredFallbackDwellScoreAdjuster(
                mutableListOf<Double>(840.0, 2079.0, 5776.0, 6437.0),
                mutableListOf<Double>(0.8, 1.18, 0.96, 1.1),
            ),
            DepotFallbackDwellScoreAdjuster(mutableMapOf<String, Double>("south" to 0.99, "online" to 0.91)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 60587.8,
    )
}
