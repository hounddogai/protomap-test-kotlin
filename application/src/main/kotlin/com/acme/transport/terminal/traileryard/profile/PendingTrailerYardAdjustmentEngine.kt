package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending trailer yard adjustment calculation. */
interface PendingTrailerYardAdjustmentModifier {
    fun describe(): String
    fun adjust(item: PendingTrailerYardAdjustment, value: Double): Double
}

data class CapPendingTrailerYardAdjustmentAdjuster(var cap: Double) : PendingTrailerYardAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTrailerYardAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredPendingTrailerYardAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTrailerYardAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTrailerYardAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingTrailerYardAdjustmentAdjuster(
    var minimum: Double,
) : PendingTrailerYardAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTrailerYardAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingTrailerYardAdjustmentAdjuster(
    var percent: Double,
) : PendingTrailerYardAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingTrailerYardAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingTrailerYardAdjustmentAdjuster(
    var step: Double,
) : PendingTrailerYardAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTrailerYardAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the pending trailer yard adjustment steps in order and keeps a trace of each. */
data class PendingTrailerYardAdjustmentEngine(
    var steps: MutableList<PendingTrailerYardAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingTrailerYardAdjustment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTrailerYardAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingTrailerYardAdjustmentEngine(): PendingTrailerYardAdjustmentEngine {
    return PendingTrailerYardAdjustmentEngine(
        steps = mutableListOf<PendingTrailerYardAdjustmentModifier>(
            CapPendingTrailerYardAdjustmentAdjuster(44289.0),
            TieredPendingTrailerYardAdjustmentAdjuster(
                mutableListOf<Double>(2805.0, 4516.0, 5685.0, 6863.0),
                mutableListOf<Double>(0.85, 0.89, 1.19, 1.01),
            ),
            MinimumPendingTrailerYardAdjustmentAdjuster(21.1),
            PercentPendingTrailerYardAdjustmentAdjuster(8.9),
            RoundingPendingTrailerYardAdjustmentAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 75717.5,
    )
}
