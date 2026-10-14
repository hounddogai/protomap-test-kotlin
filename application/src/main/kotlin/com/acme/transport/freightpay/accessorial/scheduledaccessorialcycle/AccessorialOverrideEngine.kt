package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial override calculation. */
interface AccessorialOverrideModifier {
    fun describe(): String
    fun adjust(item: AccessorialOverride, value: Double): Double
}

data class PercentAccessorialOverrideAdjuster(var percent: Double) : AccessorialOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAccessorialOverrideAdjuster(var minimum: Double) : AccessorialOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAccessorialOverrideAdjuster(var step: Double) : AccessorialOverrideModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the accessorial override steps in order and keeps a trace of each. */
data class AccessorialOverrideEngine(
    var steps: MutableList<AccessorialOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialOverride): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialOverrideEngine(): AccessorialOverrideEngine {
    return AccessorialOverrideEngine(
        steps = mutableListOf<AccessorialOverrideModifier>(
            PercentAccessorialOverrideAdjuster(18.5),
            MinimumAccessorialOverrideAdjuster(30.6),
            RoundingAccessorialOverrideAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 22814.6,
    )
}
