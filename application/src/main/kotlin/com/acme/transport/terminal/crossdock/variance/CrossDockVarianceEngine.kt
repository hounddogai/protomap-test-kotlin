package com.acme.transport.terminal.crossdock.variance

/** One step of the cross dock variance calculation. */
interface CrossDockVarianceStep {
    fun describe(): String
    fun adjust(item: CrossDockVariance, value: Double): Double
}

data class NameCrossDockVarianceAdjuster(var factors: MutableMap<String, Double>) : CrossDockVarianceStep {
    override fun describe(): String = "by name"

    override fun adjust(item: CrossDockVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCrossDockVarianceAdjuster(var minimum: Double) : CrossDockVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCrossDockVarianceAdjuster(var percent: Double) : CrossDockVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cross dock variance steps in order and keeps a trace of each. */
data class CrossDockVarianceEngine(
    var steps: MutableList<CrossDockVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CrossDockVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCrossDockVarianceEngine(): CrossDockVarianceEngine {
    return CrossDockVarianceEngine(
        steps = mutableListOf<CrossDockVarianceStep>(
            NameCrossDockVarianceAdjuster(mutableMapOf<String, Double>("north" to 0.97, "central" to 1.34)),
            MinimumCrossDockVarianceAdjuster(15.4),
            PercentCrossDockVarianceAdjuster(-23.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 98768.5,
    )
}
