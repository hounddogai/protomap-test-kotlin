package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop exception calculation. */
interface DropExceptionAdjuster {
    fun describe(): String
    fun adjust(item: DropException, value: Double): Double
}

data class RoundingDropExceptionAdjuster(var step: Double) : DropExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropException, value: Double): Double = roundTo(value, step)
}

data class SourceDropExceptionAdjuster(var factors: MutableMap<String, Double>) : DropExceptionAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: DropException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDropExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDropExceptionAdjuster(var percent: Double) : DropExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDropExceptionAdjuster(var minimum: Double) : DropExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the drop exception steps in order and keeps a trace of each. */
data class DropExceptionEngine(
    var steps: MutableList<DropExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropException): Double {
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

    fun evaluateAll(items: MutableList<DropException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropExceptionEngine(): DropExceptionEngine {
    return DropExceptionEngine(
        steps = mutableListOf<DropExceptionAdjuster>(
            RoundingDropExceptionAdjuster(0.5),
            SourceDropExceptionAdjuster(mutableMapOf<String, Double>("east" to 0.85)),
            TieredDropExceptionAdjuster(
                mutableListOf<Double>(1189.0, 4703.0, 6064.0, 8444.0),
                mutableListOf<Double>(0.81, 1.15, 1.14, 1.19),
            ),
            PercentDropExceptionAdjuster(9.7),
            MinimumDropExceptionAdjuster(36.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 19617.2,
    )
}
