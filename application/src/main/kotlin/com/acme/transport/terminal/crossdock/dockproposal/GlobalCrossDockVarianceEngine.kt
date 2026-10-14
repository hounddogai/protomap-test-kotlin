package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the global cross dock variance calculation. */
interface GlobalCrossDockVarianceFactor {
    fun describe(): String
    fun adjust(item: GlobalCrossDockVariance, value: Double): Double
}

data class CapGlobalCrossDockVarianceAdjuster(var cap: Double) : GlobalCrossDockVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCrossDockVariance, value: Double): Double = minOf(value, cap)
}

data class GlobalCrossDockVarianceExpiresAtOffsetAdjuster(var weight: Double) : GlobalCrossDockVarianceFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: GlobalCrossDockVariance, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class DepotGlobalCrossDockVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCrossDockVarianceFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalCrossDockVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalCrossDockVarianceAdjuster(var step: Double) : GlobalCrossDockVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCrossDockVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the global cross dock variance steps in order and keeps a trace of each. */
data class GlobalCrossDockVarianceEngine(
    var steps: MutableList<GlobalCrossDockVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalCrossDockVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCrossDockVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCrossDockVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalCrossDockVarianceEngine(): GlobalCrossDockVarianceEngine {
    return GlobalCrossDockVarianceEngine(
        steps = mutableListOf<GlobalCrossDockVarianceFactor>(
            CapGlobalCrossDockVarianceAdjuster(26680.0),
            GlobalCrossDockVarianceExpiresAtOffsetAdjuster(1.3),
            DepotGlobalCrossDockVarianceAdjuster(mutableMapOf<String, Double>("north" to 0.81, "west" to 0.73)),
            RoundingGlobalCrossDockVarianceAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 66771.0,
    )
}
