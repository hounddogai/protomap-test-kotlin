package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted drop exception calculation. */
interface WeightedDropExceptionAdjuster {
    fun describe(): String
    fun adjust(item: WeightedDropException, value: Double): Double
}

data class RoundingWeightedDropExceptionAdjuster(var step: Double) : WeightedDropExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDropException, value: Double): Double = roundTo(value, step)
}

data class WeightedDropExceptionAmountOffsetAdjuster(var weight: Double) : WeightedDropExceptionAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: WeightedDropException, value: Double): Double = value + item.amount * weight
}

data class PercentWeightedDropExceptionAdjuster(var percent: Double) : WeightedDropExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDropException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedDropExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDropExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDropException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted drop exception steps in order and keeps a trace of each. */
data class WeightedDropExceptionEngine(
    var steps: MutableList<WeightedDropExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedDropException): Double {
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

    fun evaluateAll(items: MutableList<WeightedDropException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedDropExceptionEngine(flags: Registry): WeightedDropExceptionEngine {
    return WeightedDropExceptionEngine(
        steps = mutableListOf<WeightedDropExceptionAdjuster>(
            RoundingWeightedDropExceptionAdjuster(0.25),
            WeightedDropExceptionAmountOffsetAdjuster(1.63),
            PercentWeightedDropExceptionAdjuster(-22.6),
            FlaggedWeightedDropExceptionAdjuster(flags, "weighted-drop-exception-adaptive", -5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 17297.4,
    )
}
