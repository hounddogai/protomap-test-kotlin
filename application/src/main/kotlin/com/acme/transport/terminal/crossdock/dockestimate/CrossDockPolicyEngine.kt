package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock policy calculation. */
interface CrossDockPolicyAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockPolicy, value: Double): Double
}

data class MinimumCrossDockPolicyAdjuster(var minimum: Double) : CrossDockPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCrossDockPolicyAdjuster(var cap: Double) : CrossDockPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredCrossDockPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCrossDockPolicyAdjuster(var percent: Double) : CrossDockPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCrossDockPolicyAdjuster(var step: Double) : CrossDockPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the cross dock policy steps in order and keeps a trace of each. */
data class CrossDockPolicyEngine(
    var steps: MutableList<CrossDockPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockPolicy): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockPolicyEngine(): CrossDockPolicyEngine {
    return CrossDockPolicyEngine(
        steps = mutableListOf<CrossDockPolicyAdjuster>(
            MinimumCrossDockPolicyAdjuster(38.7),
            CapCrossDockPolicyAdjuster(1772.0),
            TieredCrossDockPolicyAdjuster(
                mutableListOf<Double>(4450.0, 5409.0, 9348.0),
                mutableListOf<Double>(0.94, 0.97, 1.17),
            ),
            PercentCrossDockPolicyAdjuster(6.5),
            RoundingCrossDockPolicyAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 66402.8,
    )
}
