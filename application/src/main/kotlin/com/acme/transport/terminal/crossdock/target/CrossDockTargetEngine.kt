package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock target calculation. */
interface CrossDockTargetAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockTarget, value: Double): Double
}

data class TieredCrossDockTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCrossDockTargetAdjuster(var cap: Double) : CrossDockTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockTarget, value: Double): Double = minOf(value, cap)
}

data class DepotCrossDockTargetAdjuster(var factors: MutableMap<String, Double>) : CrossDockTargetAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: CrossDockTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCrossDockTargetAdjuster(var step: Double) : CrossDockTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the cross dock target steps in order and keeps a trace of each. */
data class CrossDockTargetEngine(
    var steps: MutableList<CrossDockTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockTargetEngine(): CrossDockTargetEngine {
    return CrossDockTargetEngine(
        steps = mutableListOf<CrossDockTargetAdjuster>(
            TieredCrossDockTargetAdjuster(
                mutableListOf<Double>(3846.0, 5630.0, 6535.0, 9461.0),
                mutableListOf<Double>(0.97, 1.18, 1.19, 1.09),
            ),
            CapCrossDockTargetAdjuster(13580.0),
            DepotCrossDockTargetAdjuster(mutableMapOf<String, Double>("central" to 1.38)),
            RoundingCrossDockTargetAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 55354.3,
    )
}
