package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial adjustment calculation. */
interface AccessorialAdjustmentModifier {
    fun describe(): String
    fun adjust(item: AccessorialAdjustment, value: Double): Double
}

data class RoundingAccessorialAdjustmentAdjuster(var step: Double) : AccessorialAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentAccessorialAdjustmentAdjuster(var percent: Double) : AccessorialAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAccessorialAdjustmentAdjuster(var minimum: Double) : AccessorialAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial adjustment steps in order and keeps a trace of each. */
data class AccessorialAdjustmentEngine(
    var steps: MutableList<AccessorialAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AccessorialAdjustment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAccessorialAdjustmentEngine(): AccessorialAdjustmentEngine {
    return AccessorialAdjustmentEngine(
        steps = mutableListOf<AccessorialAdjustmentModifier>(
            RoundingAccessorialAdjustmentAdjuster(5.0),
            PercentAccessorialAdjustmentAdjuster(-16.1),
            MinimumAccessorialAdjustmentAdjuster(42.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 89478.5,
    )
}
